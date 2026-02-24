package com.example.myfilimapp.ui.screens.music_screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.R
import com.example.myfilimapp.model.request.AudioItem
import com.example.myfilimapp.navigation.NavigationScreen
import com.example.myfilimapp.ui.components.CommonText
import com.example.myfilimapp.ui.state.AudioUiState
import com.example.myfilimapp.utility.AppFonts
import com.example.myfilimapp.utility.Utils.Companion.formatTime
import com.example.myfilimapp.viewmodel.PlaybackViewModel

@Composable
fun MusicScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel: PlaybackViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saveLogin(true)
    }



    when (val state = uiState) {
        is AudioUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AudioUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::refresh) { Text("Retry") }
                }
            }
        }

        is AudioUiState.Success -> {
            LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.items, key = { _, item -> item.assetPath }) { index, audio ->
                    SongRow(
                        audio,
                        onPlay = { uri->
                            navController.navigate(
                                NavigationScreen.MusicPlayerScreen.createRoute(uri,index.toString())
                            ) {
                                popUpTo(NavigationScreen.HomeScreen.route) { inclusive = false }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SongRow(
    audio    : AudioItem,
    onPlay   : (String) -> Unit,
) {
    val color = LocalCustomColors.current
    val artworkGradient : List<Color> = listOf(color.PlayingTitle, color.PlayingBorder)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color.GlassWhite)
            .border(1.dp, color.PlayingBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = {onPlay(audio.assetPath)} )
            .padding(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ArtworkThumbnail(
            artwork   = audio.artwork,
            isPlaying = false,
            gradient = artworkGradient
        )

        Column(modifier = Modifier.weight(1f)) {
            CommonText(
                text = audio.title,
                fontSize = 12.sp,
                fontFamily = AppFonts.fontInterRegular
            )
            Spacer(Modifier.height(2.dp))
            CommonText(
                text = audio.artist,
                fontSize = 12.sp,
                fontFamily = AppFonts.fontInterRegular
            )
        }

        CommonText(
            text = audio.durationMs.formatTime(),
            fontSize = 12.sp,
            fontFamily = AppFonts.fontInterRegular
        )
    }
}

@Composable
private fun ArtworkThumbnail(
    artwork  : Bitmap?,
    gradient : List<Color>,
    isPlaying: Boolean,
    modifier : Modifier = Modifier,
) {
    Box(
        modifier         = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (artwork != null) {
            SubcomposeAsyncImage(
                model              = artwork,
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Text("🎵", fontSize = 20.sp)
        }

        if (isPlaying) {
            EqBars(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp))
        }
    }
}
@Composable
private fun EqBars(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val heights = (0..2).map { i ->
        infiniteTransition.animateFloat(
            initialValue  = 4f,
            targetValue   = 11f,
            animationSpec = infiniteRepeatable(
                animation  = tween(600 + i * 120, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "eq_bar_$i"
        )
    }
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment     = Alignment.Bottom
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    }
}