package com.example.gamelibrary.model

import com.google.gson.annotations.SerializedName

data class ComicVineSearchResponse(
    // (The IIE, 2026) (Square, n.d.)
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("number_of_total_results") val totalResults: Int,
    val results: List<ComicVineVolume>
)

data class ComicVineDetailResponse(
    @SerializedName("status_code") val statusCode: Int,
    val results: ComicVineVolume
)

data class ComicVineVolume(
    val id: Int,
    @SerializedName("start_year") val startYear: String?,
    val name: String,
    val deck: String?,
    val image: ComicVineImage?,
    val publisher: ComicVinePublisher?,
    @SerializedName("count_of_issues") val issueCount: Int?,
    @SerializedName("site_detail_url") val detailUrl: String?
)

data class ComicVineImage(
    @SerializedName("medium_url") val mediumUrl: String?,
    @SerializedName("super_url") val superUrl: String?,
    @SerializedName("thumb_url") val thumbUrl: String?
)

data class ComicVinePublisher(val id: Int, val name: String)

/* Reference List
The IIE. 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
Square (n.d.) Retrofit. Available at: https://square.github.io/retrofit/ (Accessed: 15 September 2026).
*/