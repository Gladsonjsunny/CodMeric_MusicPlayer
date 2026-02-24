package com.example.myfilimapp.repository

import android.content.Context
import com.example.myfilimapp.model.request.AudioItem
import com.example.myfilimapp.utility.media.MediaMetadataExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val extractor: MediaMetadataExtractor
) {
    /** Cached result — avoids re-scanning assets on every recomposition */
    private var cachedAudio: List<AudioItem>? = null

    suspend fun getAudioItems(
        forceRefresh: Boolean = false
    ): List<AudioItem> {
        if (!forceRefresh) cachedAudio?.let { return it }
        return extractor.extractAll(context).also { cachedAudio = it }
    }
}