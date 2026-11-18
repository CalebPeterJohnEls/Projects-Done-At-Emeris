package com.example.gamelibrary.api

import com.example.gamelibrary.model.ComicVineDetailResponse
import com.example.gamelibrary.model.ComicVineSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ComicVineApiService {
    @GET("search/")
    suspend fun searchVolumes(
        @Query("query") query: String,
        @Query("resources") resources: String = "volume",
        @Query("limit") limit: Int = 20
    ): Response<ComicVineSearchResponse>

    @GET("volume/{guid}/")
    suspend fun getVolumeDetails(
        @Path("guid") guid: String
    ): Response<ComicVineDetailResponse>
}