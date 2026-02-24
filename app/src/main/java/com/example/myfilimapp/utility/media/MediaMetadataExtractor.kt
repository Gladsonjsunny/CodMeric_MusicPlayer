package com.example.myfilimapp.utility.media

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.myfilimapp.model.request.AudioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaMetadataExtractor @Inject constructor() {

    /**
     * Extracts metadata from a single asset file.
     * Must be called on a background dispatcher (IO).
     * Uses try-finally to guarantee retriever.release() even on error.
     */
    suspend fun extract(context: Context, assetFileName: String): AudioItem =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                context.assets.openFd(assetFileName).use { afd ->
                    retriever.setDataSource(
                        afd.fileDescriptor,
                        afd.startOffset,
                        afd.length
                    )
                }

                val title = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?.takeIf { it.isNotBlank() }
                    ?: assetFileName.removeSuffix(".mp3")

                val artist = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown Artist"

                val durationMs = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L

                val artwork = retriever.embeddedPicture?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }


                AudioItem(
                    assetPath  = assetFileName,
                    title      = title,
                    artist     = artist,
                    durationMs = durationMs,
                    artwork    = artwork
                )
            } finally {
                // Always released — even if an exception is thrown above
                retriever.release()
            }
        }

    /**
     * Extracts metadata for all .mp3 files found in assets root.
     * Files that fail individually are skipped (logged), not crashing the whole list.
     */
    suspend fun extractAll(context: Context): List<AudioItem> =
        withContext(Dispatchers.IO) {
            val files = context.assets.list("")
                ?.filter { it.endsWith(".mp3", ignoreCase = true) }
                ?: emptyList()

            // async + awaitAll: all files extracted concurrently on IO pool
            files
                .map { fileName ->
                    async {
                        runCatching { extract(context, fileName) }
                            .onFailure { Log.w("Extractor", "Skipping $fileName: ${it.message}") }
                            .getOrNull()
                    }
                }
                .awaitAll()
                .filterNotNull()
        }
}