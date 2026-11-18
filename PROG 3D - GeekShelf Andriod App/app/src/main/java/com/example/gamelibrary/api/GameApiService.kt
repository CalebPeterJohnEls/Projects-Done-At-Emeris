package com.example.gamelibrary.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApiService {
    // (Giant Bomb (n.d.))
    @GET("search/")
    suspend fun searchGames(
        @Query("query") query: String,
        @Query("resources") resources: String = "game",
        @Query("limit") limit: Int = 20
    ): Response<GiantBombSearchResponse>

    @GET("game/{guid}/")
    suspend fun getGameDetails(
        @Path("guid") guid: String
    ): Response<GiantBombDetailResponse>
}

data class GiantBombSearchResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("number_of_total_results") val totalResults: Int,
    val results: List<GiantBombGame>
)

data class GiantBombDetailResponse(
    @SerializedName("status_code") val statusCode: Int,
    val results: GiantBombGame
)

data class GiantBombGame(
    val id: Int,
    val guid: String,
    val name: String,
    val deck: String?, // short description
    val image: GiantBombImage?,
    @SerializedName("original_release_date") val releaseDate: String?,
    val platforms: List<GiantBombPlatform>?,
    @SerializedName("site_detail_url") val detailUrl: String?
)

data class GiantBombImage(
    @SerializedName("medium_url") val mediumUrl: String?,
    @SerializedName("super_url") val superUrl: String?, // highest-res, good for cover art
    @SerializedName("thumb_url") val thumbUrl: String?
)

data class GiantBombPlatform(
    val id: Int,
    val name: String,
    val abbreviation: String?
)

/* Reference List:
* Giant Bomb (n.d.) Giant Bomb API. Available at: https://www.giantbomb.com/api/documentation (Accessed: 15 September 2026).
*/