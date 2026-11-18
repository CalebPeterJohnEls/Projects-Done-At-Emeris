package com.example.gamelibrary.data

import com.example.gamelibrary.api.RetrofitClient
import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType
import java.io.IOException

class ComicSearchRepository : MediaSearchRepository {
    // (The IIE, 2026)
    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaSearchResult>> {
        return try {
            val response = RetrofitClient.comicVineApiService.searchVolumes(query = query)
            if (response.isSuccessful) {
                val volumes = response.body()?.results ?: emptyList()
                Result.success(volumes.map {
                    MediaSearchResult(
                        externalId = it.id.toString(),
                        title = it.name,
                        imageUrl = it.image?.superUrl,
                        subtitle = it.startYear?.let { year -> "$year · ${it.publisher?.name ?: "Unknown publisher"}" },
                        mediaType = MediaType.COMIC_BOOK
                    )
                })
            } else {
                Result.failure(Exception("Comic Vine API error: ${response.code()}"))
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