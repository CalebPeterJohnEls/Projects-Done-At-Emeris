package com.example.gamelibrary.model

import com.google.gson.annotations.SerializedName

data class TmdbMovieSearchResponse(
    // (Square, n.d.) (The IIE. 2026)
    val page: Int,
    val results: List<TmdbMovie>,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbMovie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val overview: String?
)

data class TmdbTvSearchResponse(
    val page: Int,
    val results: List<TmdbTvShow>,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbTvShow(
    val id: Int,
    val name: String, // note: "name" not "title" for TV
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    val overview: String?
)

/* Reference List
The IIE. 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
Square (n.d.) Retrofit. Available at: https://square.github.io/retrofit/ (Accessed: 15 September 2026).
*/