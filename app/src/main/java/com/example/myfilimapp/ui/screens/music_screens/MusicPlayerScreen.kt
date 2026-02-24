package com.example.myfilimapp.ui.screens.music_screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.adure.hassabi01.core.colors.CustomColors
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.R
import com.example.myfilimapp.model.request.EqPreset
import com.example.myfilimapp.model.request.MusicPlayerUiState
import com.example.myfilimapp.ui.components.BandColumn
import com.example.myfilimapp.ui.components.CommonText
import com.example.myfilimapp.utility.AppFonts
import com.example.myfilimapp.utility.Utils.Companion.toTimeString
import com.example.myfilimapp.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun MusicPlayerScreen(
    navController: NavHostController,
    assetPath : String?=null,
    index : String?=null,
    modifier: Modifier = Modifier
) {
    val viewModel : MusicPlayerViewModel = hiltViewModel()


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val waveform by viewModel.waveform.collectAsStateWithLifecycle()
    SideEffect {
        Log.e("","")
    }

    LaunchedEffect(index) {
        index?.toIntOrNull()?.let { viewModel.playSongAt(it) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        PlayerContent(
            uiState    = uiState,
            waveform   = waveform,
            viewModel  = viewModel,
            onToggle   = viewModel::togglePlayPause,
            onSeek     = viewModel::seekTo,
            onFavorite = viewModel::toggleFavorite,
            onNext     = viewModel::next,
            onPrevious = viewModel::previous,
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color(0xFF764BA2))
        }
        if (uiState.error != null) {
            ErrorState(uiState.error!!)
        }
    }
}

// ─── Player Content ───────────────────────────────────────────────────────────
@Composable
private fun PlayerContent(
    uiState    : MusicPlayerUiState,
    waveform    : List<Float>,
    viewModel  : MusicPlayerViewModel,
    onToggle   : () -> Unit,
    onSeek     : (Float) -> Unit,
    onFavorite : () -> Unit,
    onNext     : () -> Unit,
    onPrevious : () -> Unit
) {

    var showEqualizer by remember { mutableStateOf(false) }
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        AlbumArtwork(bitmap = uiState.artwork)
        Spacer(Modifier.height(32.dp))
        SongInfoRow(
            title      = uiState.title,
            artist     = uiState.artist,
            isFavorite = uiState.isFavorite,
            onFavorite = onFavorite
        )
        Spacer(Modifier.height(28.dp))
        ProgressSection(
            progress   = uiState.progress,
            currentMs  = uiState.currentMs,
            durationMs = uiState.durationMs,
            onSeek     = onSeek,
            waveform    = waveform
        )
        Spacer(Modifier.height(36.dp))
        ControlsRow(
            isPlaying = uiState.isPlaying,
            onToggle = onToggle,
            onNext= onNext,
            onPrevious = onPrevious,
            onEqualizer = {showEqualizer = true}
        )
    }
    if (showEqualizer) {
        EqualizerBottomSheet(
            viewModel = viewModel,
            onDismiss = { showEqualizer = false })
    }
}

// ─── Album Artwork ────────────────────────────────────────────────────────────
@Composable
private fun AlbumArtwork(bitmap: Bitmap?) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier         = Modifier
            .width(300.dp)
            .height(350.dp)
            .shadow(elevation = 20.dp, shape = shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap.asImageBitmap(),
                contentDescription = "Album Art",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2),
                                Color(0xFFF64F59)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.music),
                        contentDescription = "Previous",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// ─── Song Info Row ────────────────────────────────────────────────────────────
@Composable
private fun SongInfoRow(
    title      : String,
    artist     : String,
    isFavorite : Boolean,
    onFavorite : () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            CommonText(
                text       = title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A2E),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            CommonText(
                text     = artist,
                fontSize = 13.sp,
                color    = Color(0xFF9A9AB0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF0F5))
                .clickable { onFavorite() },
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = if (isFavorite)  painterResource(R.drawable.favories_filled) else painterResource(R.drawable.heart),
                contentDescription = "Previous",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Progress Section ─────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(
    progress   : Float,
    currentMs  : Long,
    durationMs : Long,
    waveform    : List<Float>,
    onSeek     : (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Waveform background - full width
            WaveformSlider(
                progress      = progress,
                waveform      =waveform,
                onSeek        = {},
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                activeColor   = Color(0xFF764BA2),
                inactiveColor = Color(0xFFEBEBF5),
            )

            // Slider on top - remove internal padding so thumb aligns with waveform edges
            Slider(
                value         = progress,
                onValueChange = onSeek,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .layout { measurable, constraints ->
                        // Force slider to measure WITHOUT thumb offset padding
                        val placeable = measurable.measure(
                            constraints.copy(
                                maxWidth = constraints.maxWidth
                            )
                        )
                        layout(placeable.width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    },
                colors        = SliderDefaults.colors(
                    thumbColor         = Color(0xFF764BA2),
                    activeTrackColor   = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor    = Color.Transparent,
                    inactiveTickColor  = Color.Transparent,
                )
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CommonText(currentMs.toTimeString(),  fontSize = 12.sp, color = Color(0xFF9A9AB0))
            CommonText(durationMs.toTimeString(), fontSize = 12.sp, color = Color(0xFF9A9AB0))
        }
    }
}



@Composable
fun WaveformSlider(
    progress     : Float,
    onSeek       : (Float) -> Unit,
    waveform     : List<Float>,
    modifier     : Modifier = Modifier,
    activeColor  : Color = Color(0xFF764BA2),
    inactiveColor: Color = Color(0xFFEBEBF5),
    barWidth     : Dp = 3.dp,
    barSpacing   : Dp = 3.dp,
    maxBarHeight : Dp = 48.dp,
    minBarHeight : Dp = 6.dp,
) {
    val targetHeights = remember(waveform) {
        waveform.ifEmpty {
            List(50) { i ->
                (sin(i * 0.4f) * 0.3f + sin(i * 0.9f) * 0.25f +
                        sin(i * 1.7f) * 0.2f + sin(i * 3.1f) * 0.15f + 0.5f)
                    .coerceIn(0.1f, 1f)
            }
        }
    }

    val barCount = targetHeights.size

    // Initialize each Animatable directly at the first target height — no blink on first render
    val animatedHeights = remember(barCount) {
        List(barCount) { i -> Animatable(targetHeights[i]) }
    }

    // On waveform change (not first load): animate smoothly to new heights
    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(targetHeights) {
        if (isFirstLoad) {
            // First load: already initialized at correct values, nothing to do
            isFirstLoad = false
            return@LaunchedEffect
        }
        // Subsequent songs: animate from current → new heights
        animatedHeights.forEachIndexed { i, anim ->
            launch {
                anim.animateTo(
                    targetValue   = targetHeights[i],
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thumbRadiusPx  = 10.dp.toPx()
            val totalBarWidth  = barWidth.toPx()
            val totalSpacing   = barSpacing.toPx()
            val step           = totalBarWidth + totalSpacing
            val maxH           = maxBarHeight.toPx()
            val minH           = minBarHeight.toPx()
            val drawableWidth  = size.width - (thumbRadiusPx * 2)
            val totalBarsWidth = step * barCount - totalSpacing
            val startX         = thumbRadiusPx + (drawableWidth - totalBarsWidth) / 2f
            val centerY        = size.height / 2f
            val trackHeight    = 2.dp.toPx()

            drawRoundRect(
                color        = inactiveColor,
                topLeft      = Offset(thumbRadiusPx, centerY - trackHeight / 2f),
                size         = Size(drawableWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f)
            )
            drawRoundRect(
                color        = activeColor,
                topLeft      = Offset(thumbRadiusPx, centerY - trackHeight / 2f),
                size         = Size(drawableWidth * progress, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f)
            )

            animatedHeights.forEachIndexed { i, anim ->
                val barProgress = i.toFloat() / barCount
                val isActive    = barProgress <= progress
                val barH        = (anim.value * maxH).coerceAtLeast(minH).coerceAtMost(maxH)
                val x           = startX + i * step
                val topY        = centerY - barH / 2f

                drawRoundRect(
                    color        = if (isActive) activeColor else inactiveColor,
                    topLeft      = Offset(x, topY),
                    size         = Size(totalBarWidth, barH),
                    cornerRadius = CornerRadius(totalBarWidth / 2f)
                )
            }
        }
    }
}
// ─── Controls Row ─────────────────────────────────────────────────────────────
@Composable
private fun ControlsRow(
    isPlaying : Boolean,
    onToggle  : () -> Unit,
    onNext     : () -> Unit,      // ← add
    onPrevious : () -> Unit,
    onEqualizer : () ->Unit
) {

    val color = LocalCustomColors.current
    val context = LocalContext.current
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        SmallControlBtn (onClick = {
            Toast.makeText(context,"Shuffle coming soon!",Toast.LENGTH_SHORT).show()}
        ){
            Image(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Shuffle",
                 modifier = Modifier.size(18.dp)
            )
        }
        SmallControlBtn (onClick = onPrevious){
            Image(
                painter = painterResource(R.drawable.previous),
                contentDescription = "Previous",
                modifier = Modifier.size(18.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    )
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = if (isPlaying) painterResource(R.drawable.pause_button)
                else
                    painterResource(R.drawable.play_button)
                ,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(25.dp),
                colorFilter = ColorFilter.tint(color = color.commonWhiteColor)
            )
        }
        SmallControlBtn (onClick = onNext){
            Image(
                painter = painterResource(R.drawable.next_button),
                contentDescription = "Previous",
                modifier = Modifier.size(18.dp)
            )
        }
        SmallControlBtn (onClick = onEqualizer){
            Image(
                painter = painterResource(R.drawable.equalizer),
                contentDescription = "Previous",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    viewModel: MusicPlayerViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val color = LocalCustomColors.current

    val eqState by viewModel.eqState.collectAsStateWithLifecycle()

    var bassAngle   by remember { mutableStateOf(0f) }
    var trebleAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        bassAngle   = eqState.bassDb   / 12f * 135f
        trebleAngle = eqState.trebleDb / 12f * 135f
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = color.commonWhiteColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color.equBandsBg)
                )
            }
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title + Close
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                CommonText(
                    text = "EQUALIZER",
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFonts.fontInterMedium,
                    fontSize = 12.sp,
                    color = color.appCommonColor,
                    modifier = Modifier.align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color.equBandsBg)
                        .clickable {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDismiss() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CommonText("✕",
                        fontSize = 12.sp,
                        color = color.appCommonColor,
                        fontFamily = AppFonts.fontInterRegular,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top section — fixed height, no overflow
            Row(
                modifier = Modifier.fillMaxWidth().height(210.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BandsPanel(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    gains = eqState.gains,
                    onGainChange = { index, value ->
//                        gains = gains.toMutableList().also { it[index] = value }
//                        activePreset = null
                        viewModel.onEqBandChanged(index, value)
                    },
                    color
                )
                Column(
                    modifier = Modifier.width(72.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KnobControl(
                        label = "BASS",
                        angle = bassAngle,
                        db = eqState.bassDb,
                        onDrag = { delta ->
//                            bassAngle = (bassAngle + delta).coerceIn(-135f, 135f)
//                            bassDb = bassAngle / 135f * 12f
                            bassAngle = (bassAngle + delta).coerceIn(-135f, 135f)
                            viewModel.onBassChanged(bassAngle / 135f * 12f)  // ✅ → audio + persists
                        },
                        color
                    )
                    KnobControl(
                        label = "TREBLE", angle = trebleAngle, db = eqState.trebleDb,
                        onDrag = { delta ->
//                            trebleAngle = (trebleAngle + delta).coerceIn(-135f, 135f)
//                            trebleDb = trebleAngle / 135f * 12f
                            trebleAngle = (trebleAngle + delta).coerceIn(-135f, 135f)
                            viewModel.onTrebleChanged(trebleAngle / 135f * 12f)  //
                        },color
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = color.ZeroLineCol, thickness = 1.dp)
            Spacer(Modifier.height(13.dp))

            CommonText(
                text = "PRESETS",
                fontFamily = AppFonts.fontInterRegular,
                fontSize = 8.sp,
                color = color.appCommonColor
            )
            Spacer(Modifier.height(10.dp))

            PresetButtons(
                activePreset = eqState.activePreset,
                onSelect = { preset ->
//                    activePreset = preset
//                    gains = preset.gains.toList()
                    viewModel.onEqPresetSelected(preset)
                },color
            )
        }
    }
}

// ── Bands Panel ──
@Composable
fun BandsPanel(
    modifier: Modifier,
    gains: List<Float>,
    onGainChange: (Int, Float) -> Unit,
    color: CustomColors
) {
   val bandLabels = listOf("60Hz", "230Hz", "1kHz", "3.5kHz", "10kHz")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.equBandsBg)
            .border(1.dp, color.ZeroLineCol, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(1.dp)
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
                .background(color.ZeroLineCol)
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bandLabels.forEachIndexed { index, label ->
                BandColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    label = label,
                    gain = gains[index],
                    onGainChange = { onGainChange(index, it) },color
                )
            }
        }
    }
}


// ── Knob ──
@Composable
fun KnobControl(
    label: String,
    angle: Float,
    db: Float,
    onDrag: (Float) -> Unit,
    color: CustomColors
) {
    val displayDb = db.roundToInt()
    val currentOnDrag by rememberUpdatedState(onDrag)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CommonText(
            text = if (displayDb > 0) "+$displayDb dB" else "$displayDb dB",
            fontFamily = AppFonts.fontInterRegular, fontSize = 8.sp, color = color.appCommonColor, maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(52.dp).shadow(7.dp, CircleShape).clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.nobTop, color.equKnobBot), radius = 90f))
                .border(0.5.dp, color.equKnobBorder, CircleShape)
                .rotate(angle)
                .nestedScroll(object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return Offset(0f, available.y)
                    }
                })
                .pointerInput(label) {
                    detectDragGestures { _, drag ->
                        currentOnDrag(-drag.y * 1.4f)
                    }
                },

            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier.padding(top = 7.dp)
                    .size(width = 3.dp, height = 11.dp)
                    .clip(RoundedCornerShape(2.dp)).background(color.appCommonColor)
            )
        }
        CommonText(text = label, fontFamily = AppFonts.fontInterRegular, fontSize = 8.sp,
             color = color.appCommonColor)
    }
}


@Composable
fun PresetButtons(
    activePreset: EqPreset?,
    onSelect: (EqPreset) -> Unit,
    color: CustomColors
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(EqPreset.entries) { preset ->
            val isActive = preset == activePreset
            val bg by animateColorAsState(
                targetValue = if (isActive) color.appCommonColor else color.commonWhiteColor,
                animationSpec = tween(200),
                label = "bg_${preset.name}"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else color.PlayingBorder,
                animationSpec = tween(200),
                label = "txt_${preset.name}"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .border(
                        width = 0.5.dp,
                        color = if (isActive) color.appCommonColor else color.ZeroLineCol,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(preset) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CommonText(
                    text = preset.label.uppercase(),
                    fontFamily = AppFonts.fontInterRegular,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Reusable small circular button ───────────────────────────────────────────
@Composable
private fun SmallControlBtn( onClick : () -> Unit = {},content: @Composable () -> Unit) {
    Box(
        modifier         = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFF4F4F8))
        .clickable { onClick() },
        contentAlignment = Alignment.Center,
        content          = { content() }
    )
}


@Composable
private fun ErrorState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CommonText("Error: $message", color = Color.Red, fontSize = 14.sp)
    }
}