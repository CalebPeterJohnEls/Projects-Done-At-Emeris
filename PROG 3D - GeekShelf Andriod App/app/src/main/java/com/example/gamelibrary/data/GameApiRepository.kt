package com.example.gamelibrary.data

import com.example.gamelibrary.api.GiantBombGame
import com.example.gamelibrary.api.RetrofitClient
import java.io.IOException

class GameApiRepository {
    suspend fun searchGames(query: String): Result<List<GiantBombGame>> {
        // (The IIE, 2026)
        return try {
            val response = RetrofitClient.gameApiService.searchGames(query = query)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("Giant Bomb API error: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error — check connection"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */