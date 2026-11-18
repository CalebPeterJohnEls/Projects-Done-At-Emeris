package com.example.gamelibrary.model

import com.google.gson.annotations.SerializedName

data class JikanSearchResponse<T>(
    val data: List<T>
)

data class JikanAnime(
    @SerializedName("mal_id") val malId: Int,
    val title: String,
    val images: JikanImages?,
    val year: Int?,
    val type: String?,
    val synopsis: String?
)

data class JikanManga(
    @SerializedName("mal_id") val malId: Int,
    val title: String,
    val images: JikanImages?,
    val published: JikanPublished?,
    val type: String?,
    val synopsis: String?
)

data class JikanImages(
    val jpg: JikanImageUrls?
)

data class JikanImageUrls(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?
)

data class JikanPublished(
    val from: String?
)