package com.example.gamelibrary.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamelibrary.api.GiantBombGame
import com.example.gamelibrary.data.GameApiRepository
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class GameSearchViewModel(private val repo: GameApiRepository = GameApiRepository()) : ViewModel() {
    var searchResults by mutableStateOf<List<GiantBombGame>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun search(query: String) {
        viewModelScope.launch {
            repo.searchGames(query)
                .onSuccess { searchResults = it; errorMessage = null }
                .onFailure { errorMessage = it.message }
        }
    }
}

/* Reference List
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
*/
