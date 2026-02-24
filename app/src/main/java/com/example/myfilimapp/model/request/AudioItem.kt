package com.example.myfilimapp.model.request

import android.graphics.Bitmap
import android.net.Uri

data class AudioItem(
    val assetPath: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artwork: Bitmap?
)