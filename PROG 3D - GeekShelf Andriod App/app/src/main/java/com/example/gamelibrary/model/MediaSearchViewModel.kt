package com.example.gamelibrary.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamelibrary.data.MediaSearchRepositoryFactory
import kotlinx.coroutines.launch

class MediaSearchViewModel : ViewModel() {
    // (The IIE, 2026)
    var mediaType by mutableStateOf(MediaType.GAME)
    var query by mutableStateOf("")
    var results by mutableStateOf<List<MediaSearchResult>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var selectedResult by mutableStateOf<MediaSearchResult?>(null)
        private set

    fun search() {
        viewModelScope.launch {
            MediaSearchRepositoryFactory.forType(mediaType).search(query, mediaType)
                .onSuccess { results = it; errorMessage = null }
                .onFailure { errorMessage = it.message; results = emptyList() }
        }
    }

    fun selectResult(result: MediaSearchResult) {
        selectedResult = result
    }
}

/* Reference List
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
*/