package com.example.gamelibrary.data

import com.example.gamelibrary.api.RetrofitClient
import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType
import java.io.IOException

class AnimeMangaSearchRepository : MediaSearchRepository {
    // (The IIE, 2026)
    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaSearchResult>> =
        when (mediaType) {
            MediaType.ANIME -> searchAnime(query)
            MediaType.MANGA -> searchManga(query)
            else -> Result.failure(Exception("AnimeMangaSearchRepository cannot handle $mediaType"))
        }

    private suspend fun searchAnime(query: String): Result<List<MediaSearchResult>> = try {
        val response = RetrofitClient.jikanApiService.searchAnime(query)
        if (response.isSuccessful) {
            Result.success(response.body()?.data.orEmpty().map {
                MediaSearchResult(
                    externalId = it.malId.toString(),
                    title = it.title,
                    imageUrl = it.images?.jpg?.largeImageUrl,
                    subtitle = listOfNotNull(it.type, it.year?.toString()).joinToString(" · "),
                    mediaType = MediaType.ANIME
                )
            })
        } else Result.failure(Exception("Jikan API error: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(Exception("Network error — check connection"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun searchManga(query: String): Result<List<MediaSearchResult>> = try {
        // (The IIE, 2026)
        val response = RetrofitClient.jikanApiService.searchManga(query)
        if (response.isSuccessful) {
            Result.success(response.body()?.data.orEmpty().map {
                MediaSearchResult(
                    externalId = it.malId.toString(),
                    title = it.title,
                    imageUrl = it.images?.jpg?.largeImageUrl,
                    subtitle = listOfNotNull(it.type, it.published?.from?.take(4)).joinToString(" · "),
                    mediaType = MediaType.MANGA
                )
            })
        } else Result.failure(Exception("Jikan API error: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(Exception("Network error — check connection"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */