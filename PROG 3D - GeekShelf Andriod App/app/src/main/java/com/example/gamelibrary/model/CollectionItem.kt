package com.example.gamelibrary.model

data class CollectionItem(
    val id: String = "",
    val userId: String = "",
    val mediaType: MediaType = MediaType.GAME,
    val externalId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val rating: Int = 0,
    val comments: String = "",
    val shelfId: String = "",
    val shelfPosition: Int = 0
)