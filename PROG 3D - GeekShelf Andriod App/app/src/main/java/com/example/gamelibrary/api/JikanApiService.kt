package com.example.gamelibrary.api

import com.example.gamelibrary.model.JikanAnime
import com.example.gamelibrary.model.JikanManga
import com.example.gamelibrary.model.JikanSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JikanApiService {
    // (Jikan, n.d.)
    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): Response<JikanSearchResponse<JikanAnime>>

    @GET("manga")
    suspend fun searchManga(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): Response<JikanSearchResponse<JikanManga>> // will not return bc
    // Jikan has been down since August, but is kept for PoC (Jikan, 2026)
}
/*Reference List
* Jikan (n.d.) Jikan REST API Documentation (v4). Available at: https://docs.api.jikan.moe/ (Accessed: 15 September 2026).
* Jikan (2026) 504 error on all endpoints. GitHub Issues [jikan-rest repository]. Available at: https://github.com/jikan-me/jikan-rest/issues (Accessed: 15 September 2026).
*/