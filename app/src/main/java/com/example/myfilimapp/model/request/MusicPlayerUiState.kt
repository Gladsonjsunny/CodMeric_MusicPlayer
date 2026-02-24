package com.example.myfilimapp.model.request

import android.graphics.Bitmap

// ─── UiState ──────────────────────────────────────────────────────────────────
data class MusicPlayerUiState(
    val title        : String  = "",
    val artist       : String  = "",
    val artwork      : Bitmap? = null,
    val durationMs   : Long    = 0L,
    val currentMs    : Long    = 0L,
    val isPlaying    : Boolean = false,
    val isLoading    : Boolean = false,
    val isFavorite   : Boolean = false,
    val currentIndex : Int     = 0,     // ← new
    val totalSongs   : Int     = 0,     // ← new
    val error        : String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) currentMs / durationMs.toFloat() else 0f
}

// ─── Metadata helper ──────────────────────────────────────────────────────────
data class SongMetadata(
    val title    : String,
    val artist   : String,
    val albumArt : Bitmap?
)

// ─── Extension: Int millis → "mm:ss" ─────────────────────────────────────────
fun Int.toTimeString(): String {
    val totalSeconds = this / 1000
    val minutes      = totalSeconds / 60
    val seconds      = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}