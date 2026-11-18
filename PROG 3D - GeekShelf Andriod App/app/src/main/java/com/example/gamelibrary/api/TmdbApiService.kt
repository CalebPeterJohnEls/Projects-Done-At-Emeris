package com.example.gamelibrary.api

import com.example.gamelibrary.model.TmdbMovieSearchResponse
import com.example.gamelibrary.model.TmdbTvSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    //
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<TmdbMovieSearchResponse>

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<TmdbTvSearchResponse>
}

/*Reference List:
* The Movie Database (n.d.) TMDB API Documentation. Available at: https://developer.themoviedb.org/docs(Accessed: 15 September 2026).
* */