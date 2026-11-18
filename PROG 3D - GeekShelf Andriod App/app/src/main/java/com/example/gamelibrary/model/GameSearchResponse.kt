package com.example.gamelibrary.model

import com.google.gson.annotations.SerializedName

data class GameSearchResponse(
    // (The IIE, 2026) (Square, n.d.)
    val count: Int,
    val results: List<GameSearchResult>
)

data class GameSearchResult(
    val id: Int,
    val name: String,
    @SerializedName("background_image") val coverUrl: String?,
    val platforms: List<PlatformWrapper>? = null
)

data class PlatformWrapper(val platform: PlatformInfo)
data class PlatformInfo(val id: Int, val name: String)
/* Reference List
The IIE. 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
Square (n.d.) Retrofit. Available at: https://square.github.io/retrofit/ (Accessed: 15 September 2026).
*/