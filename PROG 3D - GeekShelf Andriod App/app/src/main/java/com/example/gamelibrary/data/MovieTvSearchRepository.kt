package com.example.gamelibrary.data

import com.example.gamelibrary.api.RetrofitClient
import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType
import com.example.gamelibrary.BuildConfig
import java.io.IOException

class MovieTvSearchRepository : MediaSearchRepository {
    // (The IIE, 2026)
    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaSearchResult>> =
        when (mediaType) {
            MediaType.MOVIE -> searchMovies(query)
            MediaType.TV_SERIES -> searchTv(query)
            else -> Result.failure(Exception("MovieTvSearchRepository cannot handle $mediaType"))
        }

    private suspend fun searchMovies(query: String): Result<List<MediaSearchResult>> = try {
        // (The IIE, 2026)
        val response = RetrofitClient.tmdbApiService.searchMovies(query)
        if (response.isSuccessful) {
            Result.success(response.body()?.results.orEmpty().map {
                MediaSearchResult(
                    externalId = it.id.toString(),
                    title = it.title,
                    imageUrl = it.posterPath?.let { path -> BuildConfig.TMDB_IMAGE_BASE_URL + path },
                    subtitle = it.releaseDate?.take(4),
                    mediaType = MediaType.MOVIE
                )
            })
        } else Result.failure(Exception("TMDB API error: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(Exception("Network error — check connection"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun searchTv(query: String): Result<List<MediaSearchResult>> = try {
        // (The IIE, 2026)
        val response = RetrofitClient.tmdbApiService.searchTvShows(query)
        if (response.isSuccessful) {
            Result.success(response.body()?.results.orEmpty().map {
                MediaSearchResult(
                    externalId = it.id.toString(),
                    title = it.name,
                    imageUrl = it.posterPath?.let { path -> BuildConfig.TMDB_IMAGE_BASE_URL + path },
                    subtitle = it.firstAirDate?.take(4),
                    mediaType = MediaType.TV_SERIES
                )
            })
        } else Result.failure(Exception("TMDB API error: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(Exception("Network error — check connection"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */