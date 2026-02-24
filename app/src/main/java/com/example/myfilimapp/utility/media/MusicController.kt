package com.example.myfilimapp.utility.media

import com.example.myfilimapp.model.request.MusicCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @Inject constructor() {
    val command = MutableSharedFlow<MusicCommand>(extraBufferCapacity = 1)
}