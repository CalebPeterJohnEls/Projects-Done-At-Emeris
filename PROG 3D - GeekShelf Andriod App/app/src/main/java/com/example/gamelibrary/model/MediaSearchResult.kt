package com.example.gamelibrary.model

data class MediaSearchResult(
    val externalId: String,
    val title: String,
    val imageUrl: String?,
    val subtitle: String? = null, // release year, author, etc.
    val mediaType: MediaType
)
