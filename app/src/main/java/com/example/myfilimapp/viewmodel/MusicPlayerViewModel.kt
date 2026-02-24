package com.example.myfilimapp.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfilimapp.model.request.AudioItem
import com.example.myfilimapp.model.request.EqPreset
import com.example.myfilimapp.model.request.MusicCommand
import com.example.myfilimapp.model.request.MusicPlayerUiState
import com.example.myfilimapp.model.request.SongMetadata
import com.example.myfilimapp.repository.MusicPlayerRepository
import com.example.myfilimapp.ui.state.EqualizerState
import com.example.myfilimapp.utility.media.MusicController
import com.example.myfilimapp.utility.media.MusicPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val repository: MusicPlayerRepository,
    private val musicController: MusicController,
    @param:ApplicationContext private val context: Context
) : ViewModel()
{
    // ── UI State ──────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

    // ── Waveform ──────────────────────────────────────────────────────────
    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
    val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()

    // ── Equalizer ─────────────────────────────────────────────────────────
    private val _eqState = MutableStateFlow(EqualizerState())
    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()

    // ── Internals ─────────────────────────────────────────────────────────
    private var mediaPlayer  : MediaPlayer? = null
    private var equalizer    : Equalizer?   = null
    private var progressJob  : Job?         = null
    private var songList     : List<AudioItem> = emptyList()
    private var currentIndex : Int             = 0
    private var pendingIndex : Int?            = null

    private val targetFreqsHz = listOf(60, 230, 1000, 3500, 10000)

    // ── Init ──────────────────────────────────────────────────────────────
    init {
        // Listen to notification button commands
        viewModelScope.launch {
            musicController.command.collect { command ->
                when (command) {
                    MusicCommand.PLAY     -> play()
                    MusicCommand.PAUSE    -> pause()
                    MusicCommand.NEXT     -> next()
                    MusicCommand.PREVIOUS -> previous()
                }
            }
        }

        // Load song list
        viewModelScope.launch {
            songList = repository.getAudioItems()
            _uiState.update { it.copy(totalSongs = songList.size) }
            pendingIndex?.let { playSongAt(it); pendingIndex = null }
        }
    }

    // ── Public: Playback Controls ─────────────────────────────────────────

    fun playSongAt(index: Int) {
        if (songList.isEmpty()) { pendingIndex = index; return }
        currentIndex = index.coerceIn(0, songList.lastIndex)
        loadAndPlay(songList[currentIndex])
    }

    fun next() {
        if (songList.isEmpty()) return
        currentIndex = (currentIndex + 1) % songList.size
        loadAndPlay(songList[currentIndex])
    }

    fun previous() {
        if (songList.isEmpty()) return
        if (_uiState.value.currentMs <= 3000) {
            currentIndex = (currentIndex - 1 + songList.size) % songList.size
            loadAndPlay(songList[currentIndex])
        } else {
            seekTo(0f)
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun seekTo(fraction: Float) {
        val targetMs = (fraction * _uiState.value.durationMs).toLong()
        mediaPlayer?.seekTo(targetMs.toInt())
        _uiState.update { it.copy(currentMs = targetMs) }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onPause()  { if (_uiState.value.isPlaying) pause() }
    fun onResume() { }

    // ── Public: Equalizer Controls ────────────────────────────────────────

    fun onEqBandChanged(bandIndex: Int, gainDb: Float) {
        val newGains = _eqState.value.gains.toMutableList()
            .also { it[bandIndex] = gainDb }
        _eqState.update { it.copy(gains = newGains, activePreset = null) }
        applyBandGain(bandIndex, gainDb)
    }

    fun onEqPresetSelected(preset: EqPreset) {
        _eqState.update { it.copy(gains = preset.gains.toList(), activePreset = preset) }
        applyAllBands(preset.gains)
    }

    fun onBassChanged(db: Float) {
        _eqState.update { it.copy(bassDb = db) }
        applyBassShelf(db)
    }

    fun onTrebleChanged(db: Float) {
        _eqState.update { it.copy(trebleDb = db) }
        applyTrebleShelf(db)
    }

    fun toggleEqualizer() {
        val enabled = !_eqState.value.isEnabled
        _eqState.update { it.copy(isEnabled = enabled) }
        equalizer?.enabled = enabled
    }

    // ── Private: Load & Play ──────────────────────────────────────────────

    private fun loadAndPlay(audio: AudioItem) {
        releasePlayer()
        _waveform.value = emptyList()
        loadWaveform(audio.assetPath)

        _uiState.update {
            it.copy(
                title        = audio.title,
                artist       = audio.artist,
                artwork      = audio.artwork,
                durationMs   = audio.durationMs,
                currentMs    = 0L,
                isPlaying    = false,
                isLoading    = true,
                error        = null,
                currentIndex = currentIndex,
                totalSongs   = songList.size
            )
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val player = MediaPlayer()
                    context.assets.openFd(audio.assetPath).use { afd ->
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                    player.prepare()
                    player
                }.onSuccess { player ->
                    withContext(Dispatchers.Main) {
                        mediaPlayer = player
                        attachEqualizer(player)
                        player.setOnCompletionListener { next() }
                        _uiState.update { it.copy(isLoading = false) }
                        play()
                    }
                }.onFailure { e ->
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
            }
        }
    }

    private fun play() {
        mediaPlayer?.start()
        _uiState.update { it.copy(isPlaying = true) }
        startProgressTracking()
        syncService(isPlaying = true)
    }

    private fun pause() {
        mediaPlayer?.pause()
        progressJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
        syncService(isPlaying = false)
    }

    // Push current state to service so notification stays in sync
    private fun syncService(isPlaying: Boolean) {
        MusicPlayerService.isPlaying      = isPlaying
        MusicPlayerService.currentTitle   = _uiState.value.title
        MusicPlayerService.currentArtist  = _uiState.value.artist
        MusicPlayerService.currentArtwork = _uiState.value.artwork
        MusicPlayerService.refresh(context)
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _uiState.update { it.copy(currentMs = pos) }
                delay(500)
            }
        }
    }

    // ── Private: Equalizer ────────────────────────────────────────────────

    private fun attachEqualizer(player: MediaPlayer) {
        releaseEqualizer()
        try {
            val eq = Equalizer(0, player.audioSessionId)
            eq.enabled = _eqState.value.isEnabled
            equalizer  = eq
            applyAllBands(_eqState.value.gains)
            applyBassShelf(_eqState.value.bassDb)
            applyTrebleShelf(_eqState.value.trebleDb)
        } catch (e: Exception) {
            equalizer = null
        }
    }

    private fun applyBandGain(uiBandIndex: Int, gainDb: Float) {
        val eq = equalizer ?: return
        val hwBand = mapUiBandToHwBand(eq, uiBandIndex) ?: return
        val combined = when (uiBandIndex) {
            0 -> gainDb + _eqState.value.bassDb
            1 -> gainDb + _eqState.value.bassDb   * 0.5f
            3 -> gainDb + _eqState.value.trebleDb * 0.5f
            4 -> gainDb + _eqState.value.trebleDb
            else -> gainDb
        }
        val millibels = (combined * 100).toInt().toShort()
            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
        eq.setBandLevel(hwBand, millibels)
    }

    private fun applyAllBands(gains: List<Float>) {
        val eq = equalizer ?: return
        gains.forEachIndexed { uiBandIndex, gainDb ->
            val hwBand = mapUiBandToHwBand(eq, uiBandIndex) ?: return@forEachIndexed
            val millibels = (gainDb * 100).toInt().toShort()
                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
            eq.setBandLevel(hwBand, millibels)
        }
    }

    private fun applyBassShelf(db: Float) {
        val eq        = equalizer ?: return
        val bandCount = eq.numberOfBands.toInt()
        if (bandCount < 1) return
        val combined0 = _eqState.value.gains.getOrElse(0) { 0f } + db
        eq.setBandLevel(0, (combined0 * 100).toInt().toShort()
            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
        if (bandCount >= 2) {
            val combined1 = _eqState.value.gains.getOrElse(1) { 0f } + (db * 0.5f)
            eq.setBandLevel(1, (combined1 * 100).toInt().toShort()
                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
        }
    }

    private fun applyTrebleShelf(db: Float) {
        val eq        = equalizer ?: return
        val bandCount = eq.numberOfBands.toInt()
        if (bandCount < 1) return
        val topIndex    = bandCount - 1
        val combinedTop = _eqState.value.gains.getOrElse(4) { 0f } + db
        eq.setBandLevel(topIndex.toShort(), (combinedTop * 100).toInt().toShort()
            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
        if (bandCount >= 2) {
            val combined2nd = _eqState.value.gains.getOrElse(3) { 0f } + (db * 0.5f)
            eq.setBandLevel((topIndex - 1).toShort(), (combined2nd * 100).toInt().toShort()
                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
        }
    }

    private fun mapUiBandToHwBand(eq: Equalizer, uiBandIndex: Int): Short? {
        val targetHz      = targetFreqsHz.getOrNull(uiBandIndex) ?: return null
        val targetMilliHz = targetHz * 1000
        val bandCount     = eq.numberOfBands.toInt()
        var closest       = 0.toShort()
        var minDiff       = Int.MAX_VALUE
        for (i in 0 until bandCount) {
            val band          = i.toShort()
            val centerMilliHz = eq.getCenterFreq(band)
            val diff          = abs(centerMilliHz - targetMilliHz)
            if (diff < minDiff) { minDiff = diff; closest = band }
        }
        return closest
    }

    private fun releaseEqualizer() {
        equalizer?.release()
        equalizer = null
    }

    // ── Private: Waveform ─────────────────────────────────────────────────

    private fun loadWaveform(assetPath: String, barCount: Int = 50) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val extractor = MediaExtractor()
                context.assets.openFd(assetPath).use { afd ->
                    extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }

                var audioTrackIndex = -1
                var format: MediaFormat? = null
                for (i in 0 until extractor.trackCount) {
                    val fmt = extractor.getTrackFormat(i)
                    if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                        audioTrackIndex = i; format = fmt; break
                    }
                }
                if (audioTrackIndex == -1 || format == null) return@launch

                val durationUs = format.getLong(MediaFormat.KEY_DURATION)
                val usPerBar   = durationUs / barCount
                extractor.selectTrack(audioTrackIndex)

                val decoder = MediaCodec.createDecoderByType(
                    format.getString(MediaFormat.KEY_MIME)!!
                )
                decoder.configure(format, null, null, 0)
                decoder.start()

                val bufferInfo       = MediaCodec.BufferInfo()
                val bars             = MutableList(barCount) { 0f }
                val currentBarSamples = mutableListOf<Short>()
                var currentBar       = 0
                var inputDone        = false
                var outputDone       = false

                while (!outputDone) {
                    if (!inputDone) {
                        val inputIndex = decoder.dequeueInputBuffer(10_000)
                        if (inputIndex >= 0) {
                            val inputBuffer = decoder.getInputBuffer(inputIndex)!!
                            val sampleSize  = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(
                                    inputIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputDone = true
                            } else {
                                decoder.queueInputBuffer(
                                    inputIndex, 0, sampleSize,
                                    extractor.sampleTime, 0
                                )
                                extractor.advance()
                            }
                        }
                    }

                    val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (outputIndex >= 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputIndex)!!
                        val chunk = ShortArray(outputBuffer.remaining() / 2)
                        outputBuffer.asShortBuffer().get(chunk)
                        decoder.releaseOutputBuffer(outputIndex, false)

                        for (sample in chunk) {
                            currentBarSamples.add(sample)
                            val barBoundaryUs = (currentBar + 1) * usPerBar
                            if (bufferInfo.presentationTimeUs >= barBoundaryUs
                                && currentBar < barCount - 1) {
                                val peak = currentBarSamples
                                    .maxOfOrNull { abs(it.toInt()) } ?: 0
                                bars[currentBar] = peak.toFloat() / Short.MAX_VALUE
                                currentBarSamples.clear()
                                currentBar++
                                val snapshot = bars.toList()
                                withContext(Dispatchers.Main) { _waveform.value = snapshot }
                            }
                        }

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }

                if (currentBarSamples.isNotEmpty() && currentBar < barCount) {
                    val peak = currentBarSamples.maxOfOrNull { abs(it.toInt()) } ?: 0
                    bars[currentBar] = peak.toFloat() / Short.MAX_VALUE
                }

                decoder.stop(); decoder.release(); extractor.release()
                withContext(Dispatchers.Main) { _waveform.value = bars.toList() }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _waveform.value = emptyList() }
            }
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────

    private fun releasePlayer() {
        progressJob?.cancel()
        releaseEqualizer()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
//    // ── Player UI state ──
//    private val _uiState = MutableStateFlow(MusicPlayerUiState())
//    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()
//
//    // ── Waveform ──
//    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
//    val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()
//
//    // ── Equalizer state — persisted here, survives recomposition ──
//    private val _eqState = MutableStateFlow(EqualizerState())
//    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()
//
//    // ── Internals ──
//    private var mediaPlayer  : MediaPlayer? = null
//    private var equalizer    : Equalizer?   = null   // Android AudioEffect
//    private var progressJob  : Job?         = null
//    private var songList     : List<AudioItem> = emptyList()
//    private var currentIndex : Int             = 0
//    private var pendingIndex : Int?            = null
//
//    // ── Band frequency mapping (matches your 5-band UI labels) ──
//    // Android Equalizer gives us the actual band count & center freqs at runtime.
//    // We map our 5 UI bands → the closest HW band by center frequency.
//    private val targetFreqsHz = listOf(60, 230, 1000, 3500, 10000)
//
//    // ─────────────────────────────────────────
//    init {
//        viewModelScope.launch {
//            songList = repository.getAudioItems()
//            _uiState.update { it.copy(totalSongs = songList.size) }
//            pendingIndex?.let { playSongAt(it); pendingIndex = null }
//        }
//    }
//
//    // ─────────────────────────────────────────
//    //  Public: play controls (unchanged)
//    // ─────────────────────────────────────────
//    fun playSongAt(index: Int) {
//        if (songList.isEmpty()) { pendingIndex = index; return }
//        currentIndex = index.coerceIn(0, songList.lastIndex)
//        loadAndPlay(songList[currentIndex])
//    }
//
//    fun next() {
//        if (songList.isEmpty()) return
//        currentIndex = (currentIndex + 1) % songList.size
//        loadAndPlay(songList[currentIndex])
//    }
//
//
//
//    fun previous() {
//        if (songList.isEmpty()) return
//
//        val isNearStart = _uiState.value.currentMs <= 3000
//
//        if (isNearStart) {
//            // Already at the start of the song — go to actual previous
//            currentIndex = (currentIndex - 1 + songList.size) % songList.size
//            loadAndPlay(songList[currentIndex])
//        } else {
//            // More than 3s in — restart current song (single click = restart, double click = previous)
//            seekTo(0f)
//        }
//    }
//
//    fun togglePlayPause() { if (_uiState.value.isPlaying) pause() else play() }
//
//    fun seekTo(fraction: Float) {
//        val targetMs = (fraction * _uiState.value.durationMs).toLong()
//        mediaPlayer?.seekTo(targetMs.toInt())
//        _uiState.update { it.copy(currentMs = targetMs) }
//    }
//
//    fun toggleFavorite() { _uiState.update { it.copy(isFavorite = !it.isFavorite) } }
//    fun onPause()  { if (_uiState.value.isPlaying) pause() }
//    fun onResume() { }
//
//    // ─────────────────────────────────────────
//    //  Public: Equalizer controls
//    //  Called directly from EqualizerBottomSheet
//    // ─────────────────────────────────────────
//
//    /** User drags a band slider manually */
//    fun onEqBandChanged(bandIndex: Int, gainDb: Float) {
//        val newGains = _eqState.value.gains.toMutableList()
//            .also { it[bandIndex] = gainDb }
//        _eqState.update { it.copy(gains = newGains, activePreset = null) }
//        applyBandGain(bandIndex, gainDb)
//    }
//
//    /** User selects a preset — applies all 5 bands at once */
//    fun onEqPresetSelected(preset: EqPreset) {
//        _eqState.update {
//            it.copy(gains = preset.gains.toList(), activePreset = preset)
//        }
//        applyAllBands(preset.gains)
//    }
//
//    /** User drags the Bass knob */
//    fun onBassChanged(db: Float) {
//        _eqState.update { it.copy(bassDb = db) }
//        applyBassShelf(db)
//    }
//
//    /** User drags the Treble knob */
//    fun onTrebleChanged(db: Float) {
//        _eqState.update { it.copy(trebleDb = db) }
//        applyTrebleShelf(db)
//    }
//
//    /** Toggle EQ on/off */
//    fun toggleEqualizer() {
//        val enabled = !_eqState.value.isEnabled
//        _eqState.update { it.copy(isEnabled = enabled) }
//        equalizer?.enabled = enabled
//    }
//
//    // ─────────────────────────────────────────
//    //  Private: Apply EQ to Android Equalizer
//    // ─────────────────────────────────────────
//
//    /**
//     * Attaches a new Equalizer to the current MediaPlayer session
//     * and re-applies whatever state is saved in _eqState.
//     */
//    private fun attachEqualizer(player: MediaPlayer) {
//        releaseEqualizer()
//        try {
//            val eq = Equalizer(0, player.audioSessionId)
//            eq.enabled = _eqState.value.isEnabled
//            equalizer = eq
//            // Re-apply persisted state immediately
//            applyAllBands(_eqState.value.gains)
//            applyBassShelf(_eqState.value.bassDb)
//            applyTrebleShelf(_eqState.value.trebleDb)
//        } catch (e: Exception) {
//            // Equalizer not supported on this device/song — fail silently
//            equalizer = null
//        }
//    }
//
//    /**
//     * Apply a single band gain.
//     * gainDb: -12f..+12f  →  converted to millibels for Android API
//     */
//
//    private fun applyBandGain(uiBandIndex: Int, gainDb: Float) {
//        val eq = equalizer ?: return
//        val hwBand = mapUiBandToHwBand(eq, uiBandIndex) ?: return
//
//        // Add shelf offset if this band overlaps bass or treble
//        val combined = when (uiBandIndex) {
//            0 -> gainDb + _eqState.value.bassDb          // band 0 = full bass shelf
//            1 -> gainDb + _eqState.value.bassDb * 0.5f   // band 1 = half bass shelf
//            3 -> gainDb + _eqState.value.trebleDb * 0.5f // band 3 = half treble shelf
//            4 -> gainDb + _eqState.value.trebleDb         // band 4 = full treble shelf
//            else -> gainDb
//        }
//
//        val millibels = (combined * 100).toInt().toShort()
//            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
//        eq.setBandLevel(hwBand, millibels)
//    }
//
//    /** Apply all 5 band gains at once (used by presets) */
//    private fun applyAllBands(gains: List<Float>) {
//        val eq = equalizer ?: return
//        gains.forEachIndexed { uiBandIndex, gainDb ->
//            val hwBand = mapUiBandToHwBand(eq, uiBandIndex) ?: return@forEachIndexed
//            val millibels = (gainDb * 100).toInt().toShort()
//                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
//            eq.setBandLevel(hwBand, millibels)
//        }
//    }
//
//    /**
//     * Bass shelf — boosts/cuts the lowest 1–2 hardware bands.
//     * We treat the lowest HW band as the bass band.
//     */
//
//    private fun applyBassShelf(db: Float) {
//        val eq = equalizer ?: return
//        val bandCount = eq.numberOfBands.toInt()
//        if (bandCount < 1) return
//
//        // Band 0: bandGain[0] + bassShelf
//        val combined0 = _eqState.value.gains.getOrElse(0) { 0f } + db
//        eq.setBandLevel(0, (combined0 * 100).toInt().toShort()
//            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
//
//        // Band 1: bandGain[1] + half bassShelf
//        if (bandCount >= 2) {
//            val combined1 = _eqState.value.gains.getOrElse(1) { 0f } + (db * 0.5f)
//            eq.setBandLevel(1, (combined1 * 100).toInt().toShort()
//                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
//        }
//    }
//
//    /**
//     * Treble shelf — boosts/cuts the highest 1–2 hardware bands.
//     */
//
//    private fun applyTrebleShelf(db: Float) {
//        val eq = equalizer ?: return
//        val bandCount = eq.numberOfBands.toInt()
//        if (bandCount < 1) return
//
//        // Top band: bandGain[top] + trebleShelf
//        val topIndex = bandCount - 1
//        val combinedTop = _eqState.value.gains.getOrElse(4) { 0f } + db
//        eq.setBandLevel(topIndex.toShort(), (combinedTop * 100).toInt().toShort()
//            .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
//
//        // Second-to-top: bandGain[second] + half trebleShelf
//        if (bandCount >= 2) {
//            val combined2nd = _eqState.value.gains.getOrElse(3) { 0f } + (db * 0.5f)
//            eq.setBandLevel((topIndex - 1).toShort(), (combined2nd * 100).toInt().toShort()
//                .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1]))
//        }
//    }
//
//    /**
//     * Maps our 5 UI band indices to the closest hardware band
//     * by comparing center frequencies.
//     */
//    private fun mapUiBandToHwBand(eq: Equalizer, uiBandIndex: Int): Short? {
//        val targetHz = targetFreqsHz.getOrNull(uiBandIndex) ?: return null
//        val targetMilliHz = targetHz * 1000  // Android uses mHz
//        val bandCount = eq.numberOfBands.toInt()
//        var closest: Short = 0
//        var minDiff = Int.MAX_VALUE
//        for (i in 0 until bandCount) {
//            val band = i.toShort()
//            val centerMilliHz = eq.getCenterFreq(band)
//            val diff = Math.abs(centerMilliHz - targetMilliHz)
//            if (diff < minDiff) { minDiff = diff; closest = band }
//        }
//        return closest
//    }
//
//    private fun releaseEqualizer() {
//        equalizer?.release()
//        equalizer = null
//    }
//
//    // ─────────────────────────────────────────
//    //  Private: load + play (attach EQ after prepare)
//    // ─────────────────────────────────────────
//    private fun loadAndPlay(audio: AudioItem) {
//        releasePlayer()
//        _waveform.value = emptyList()
//        loadWaveform(audio.assetPath)
//        _uiState.update {
//            it.copy(
//                title        = audio.title,
//                artist       = audio.artist,
//                artwork      = audio.artwork,
//                durationMs   = audio.durationMs,
//                currentMs    = 0L,
//                isPlaying    = false,
//                isLoading    = true,
//                error        = null,
//                currentIndex = currentIndex,
//                totalSongs   = songList.size
//            )
//        }
//
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                runCatching {
//                    val player = MediaPlayer()
//                    context.assets.openFd(audio.assetPath).use { afd ->
//                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                    }
//                    player.prepare()
//                    player
//                }.onSuccess { player ->
//                    withContext(Dispatchers.Main) {
//                        mediaPlayer = player
//                        // ✅ Attach EQ right after prepare, before play
//                        attachEqualizer(player)
//                        player.setOnCompletionListener { next() }
//                        _uiState.update { it.copy(isLoading = false) }
//                        play()
//                    }
//                }.onFailure { e ->
//                    withContext(Dispatchers.Main) {
//                        _uiState.update { it.copy(isLoading = false, error = e.message) }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun play() {
//        mediaPlayer?.start()
//        _uiState.update { it.copy(isPlaying = true) }
//        startProgressTracking()
//    }
//
//    private fun pause() {
//        mediaPlayer?.pause()
//        progressJob?.cancel()
//        _uiState.update { it.copy(isPlaying = false) }
//    }
//
//    private fun startProgressTracking() {
//        progressJob?.cancel()
//        progressJob = viewModelScope.launch {
//            while (isActive) {
//                val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
//                _uiState.update { it.copy(currentMs = pos) }
//                delay(500)
//            }
//        }
//    }
//
//    private fun releasePlayer() {
//        progressJob?.cancel()
//        releaseEqualizer()   // ✅ always release EQ before releasing player
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        releasePlayer()
//    }
//
//    // ─────────────────────────────────────────
//    //  Waveform (unchanged from your original)
//    // ─────────────────────────────────────────
//    private fun loadWaveform(assetPath: String, barCount: Int = 50) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val extractor = MediaExtractor()
//                context.assets.openFd(assetPath).use { afd ->
//                    extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                }
//                var audioTrackIndex = -1
//                var format: MediaFormat? = null
//                for (i in 0 until extractor.trackCount) {
//                    val fmt = extractor.getTrackFormat(i)
//                    if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
//                        audioTrackIndex = i; format = fmt; break
//                    }
//                }
//                if (audioTrackIndex == -1 || format == null) return@launch
//                val durationUs = format.getLong(MediaFormat.KEY_DURATION)
//                val usPerBar = durationUs / barCount
//                extractor.selectTrack(audioTrackIndex)
//                val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
//                decoder.configure(format, null, null, 0)
//                decoder.start()
//                val bufferInfo = MediaCodec.BufferInfo()
//                val bars = MutableList(barCount) { 0f }
//                val currentBarSamples = mutableListOf<Short>()
//                var currentBar = 0
//                var inputDone = false
//                var outputDone = false
//                while (!outputDone) {
//                    if (!inputDone) {
//                        val inputIndex = decoder.dequeueInputBuffer(10_000)
//                        if (inputIndex >= 0) {
//                            val inputBuffer = decoder.getInputBuffer(inputIndex)!!
//                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
//                            if (sampleSize < 0) {
//                                decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
//                                inputDone = true
//                            } else {
//                                decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
//                                extractor.advance()
//                            }
//                        }
//                    }
//                    val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10_000)
//                    if (outputIndex >= 0) {
//                        val outputBuffer = decoder.getOutputBuffer(outputIndex)!!
//                        val chunk = ShortArray(outputBuffer.remaining() / 2)
//                        outputBuffer.asShortBuffer().get(chunk)
//                        decoder.releaseOutputBuffer(outputIndex, false)
//                        for (sample in chunk) {
//                            currentBarSamples.add(sample)
//                            val barBoundaryUs = (currentBar + 1) * usPerBar
//                            if (bufferInfo.presentationTimeUs >= barBoundaryUs && currentBar < barCount - 1) {
//                                val peak = currentBarSamples.maxOfOrNull { abs(it.toInt()) } ?: 0
//                                bars[currentBar] = peak.toFloat() / Short.MAX_VALUE
//                                currentBarSamples.clear()
//                                currentBar++
//                                val snapshot = bars.toList()
//                                withContext(Dispatchers.Main) { _waveform.value = snapshot }
//                            }
//                        }
//                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
//                    }
//                }
//                if (currentBarSamples.isNotEmpty() && currentBar < barCount) {
//                    val peak = currentBarSamples.maxOfOrNull { abs(it.toInt()) } ?: 0
//                    bars[currentBar] = peak.toFloat() / Short.MAX_VALUE
//                }
//                decoder.stop(); decoder.release(); extractor.release()
//                withContext(Dispatchers.Main) { _waveform.value = bars.toList() }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) { _waveform.value = emptyList() }
//            }
//        }
//    }




}