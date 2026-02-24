package com.example.myfilimapp.ui.state

import com.example.myfilimapp.model.request.AudioItem

sealed interface AudioUiState {
    data object Loading : AudioUiState
    data class Success(val items: List<AudioItem>) : AudioUiState
    data class Error(val message: String) : AudioUiState
}