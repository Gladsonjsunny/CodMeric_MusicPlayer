package com.example.myfilimapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfilimapp.dataStore.UserPreferences
import com.example.myfilimapp.model.request.AudioItem
import com.example.myfilimapp.repository.AudioRepository
import com.example.myfilimapp.ui.state.AudioUiState
import com.example.myfilimapp.utility.media.MediaMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val repository: AudioRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Single StateFlow — UI observes one source of truth
    private val _uiState = MutableStateFlow<AudioUiState>(AudioUiState.Loading)
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        loadAudio()
    }


    fun saveLogin( login: Boolean) {
        viewModelScope.launch {
            userPreferences.saveLogged(
                login = login
            )
        }
    }

    private fun loadAudio() {
        viewModelScope.launch {
            _uiState.value = AudioUiState.Loading
            _uiState.value = runCatching { repository.getAudioItems() }
                .fold(
                    onSuccess = { AudioUiState.Success(it) },
                    onFailure = { AudioUiState.Error(it.localizedMessage ?: "Unknown error") }
                )
        }
    }

    fun refresh() = loadAudio()


}