package com.example.myfilimapp.di

import android.content.Context
import com.example.myfilimapp.utility.media.MediaMetadataExtractor
import com.example.myfilimapp.utility.media.PlaybackController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideMetadataExtractor(): MediaMetadataExtractor {
        return MediaMetadataExtractor()
    }

    @Provides
    @Singleton
    fun providePlaybackController(
        @ApplicationContext context: Context
    ): PlaybackController {
        return PlaybackController(context)
    }
}