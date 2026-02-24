package com.example.myfilimapp.utility.media

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaybackController @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress

    fun prepare(assetPath: String) {
        release()

        val afd = context.assets.openFd(assetPath)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            prepare()
            setOnCompletionListener {
                _isPlaying.value = false
            }
        }
    }

    fun play() {
        mediaPlayer?.start()
        _isPlaying.value = true
        startProgressUpdates()
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        stopProgressUpdates()
    }
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer?.isPlaying == true) {
                _progress.value =
                    mediaPlayer?.currentPosition?.toLong() ?: 0L
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun release() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}