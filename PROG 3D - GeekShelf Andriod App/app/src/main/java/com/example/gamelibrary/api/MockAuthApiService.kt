package com.example.gamelibrary.api

import kotlinx.coroutines.delay

class MockAuthApiService : AuthApiService {

    override suspend fun verifySteam(
        request: SteamVerifyRequest
    ): LinkedAccountResponse {

        delay(600)

        return LinkedAccountResponse(
            username = "Player_${request.steamId64.takeLast(4)}",
            linkedAt = System.currentTimeMillis()
        )
    }

    override suspend fun verifyEpic(
        request: OAuthCodeRequest
    ): LinkedAccountResponse {

        delay(600)

        return LinkedAccountResponse(
            username =
                "EpicUser_${request.authorizationCode.take(4)}",

            linkedAt =
                System.currentTimeMillis()
        )
    }

    override suspend fun verifyXbox(
        request: OAuthCodeRequest
    ): LinkedAccountResponse {

        delay(600)

        return LinkedAccountResponse(
            username =
                "Gamertag${request.authorizationCode.take(4)}",

            linkedAt =
                System.currentTimeMillis()
        )
    }

    /**
     * Demo wishlist data.
     *
     * This is used when the unofficial Steam wishlist endpoint
     * cannot provide JSON.
     */
    override suspend fun getSteamWishlist(
        steamId64: String
    ): List<WishlistItemResponse> {

        delay(600)

        return listOf(

            WishlistItemResponse(
                appId = 1145360,
                name = "Hades II",
                headerImageUrl =
                    "https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/header.jpg",
                price = "$24.99"
            ),

            WishlistItemResponse(
                appId = 1030300,
                name = "Hollow Knight: Silksong",
                headerImageUrl =
                    "https://cdn.cloudflare.steamstatic.com/steam/apps/1030300/header.jpg",
                price = "$19.99"
            ),

            WishlistItemResponse(
                appId = 1086940,
                name = "Baldur's Gate 3",
                headerImageUrl =
                    "https://cdn.cloudflare.steamstatic.com/steam/apps/1086940/header.jpg",
                price = "$59.99"
            )
        )
    }
}