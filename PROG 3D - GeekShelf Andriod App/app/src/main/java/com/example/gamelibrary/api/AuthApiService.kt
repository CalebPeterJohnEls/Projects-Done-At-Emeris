package com.example.gamelibrary.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {

    @POST("auth/steam/callback")
    suspend fun verifySteam(@Body request: SteamVerifyRequest): LinkedAccountResponse

    @POST("auth/epic/callback")
    suspend fun verifyEpic(@Body request: OAuthCodeRequest): LinkedAccountResponse

    @POST("auth/xbox/callback")
    suspend fun verifyXbox(@Body request: OAuthCodeRequest): LinkedAccountResponse

    // Server holds the Steam Web API key and calls IWishlistService/GetWishlist (Valve, 2026)
    @GET("wishlist/steam/{steamId64}")
    suspend fun getSteamWishlist(@Path("steamId64") steamId64: String): List<WishlistItemResponse>
}

data class SteamVerifyRequest(val steamId64: String)
data class OAuthCodeRequest(val authorizationCode: String)
data class LinkedAccountResponse(val username: String, val linkedAt: Long)
data class WishlistItemResponse(val appId: Int, val name: String, val headerImageUrl: String, val price: String?)