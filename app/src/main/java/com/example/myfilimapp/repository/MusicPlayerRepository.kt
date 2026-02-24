package com.example.myfilimapp.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.myfilimapp.model.request.AudioItem
import com.example.myfilimapp.utility.media.MediaMetadataExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class MusicPlayerRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val extractor: MediaMetadataExtractor
) {

    suspend fun loadSong(assetPath: String): AudioItem =
        extractor.extract(context, assetPath)

    private var cachedAudio: List<AudioItem>? = null

    suspend fun getAudioItems(forceRefresh: Boolean = false): List<AudioItem> {
        if (!forceRefresh) cachedAudio?.let { return it }
        return extractor.extractAll(context).also { cachedAudio = it }
    }

}