package com.example.gamelibrary.model

import com.google.gson.annotations.SerializedName

data class GameDetails(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("background_image") val coverUrl: String?
)