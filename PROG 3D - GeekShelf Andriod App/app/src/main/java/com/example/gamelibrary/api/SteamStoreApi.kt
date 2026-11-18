package com.example.gamelibrary.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ------------------------------------------------------------
// Steam Wishlist API
//
// GetWishlist is not in Valve's own public documentation — it was found
// and documented by the community, so xPaw's generated reference is used
// here to confirm the endpoint path and response shape
// (xPaw, 2026a; Valve Corporation, 2026b).
// ------------------------------------------------------------

interface SteamStoreApi {

    // Returns the raw list of appids on a user's wishlist, most recently
    // added first, by SteamID64 (xPaw, 2026a)
    @GET("IWishlistService/GetWishlist/v1/")
    suspend fun getWishlist(
        @Query("steamid") steamId: String
    ): SteamWishlistResponse
}

// Wraps the API's response object, matching Steam's standard
// { "response": { ... } } envelope (Valve Corporation, 2026b)
data class SteamWishlistResponse(
    val response: SteamWishlistResponseData?
)

data class SteamWishlistResponseData(
    val items: List<SteamWishlistEntry>?
)

// One wishlist entry — just an appid and metadata about when it was
// added, NOT the game's name/price/image (that needs a second call,
// see SteamBrowseApi below) (xPaw, 2026a)
data class SteamWishlistEntry(
    val appid: Int,
    val priority: Int?,
    val date_added: Long?
)

// Retrofit client pointed at Steam's public API host, built the same way
// as the other Retrofit clients in RetrofitClient.kt (Square, 2026)
object SteamStoreClient {

    val api: SteamStoreApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamStoreApi::class.java)
    }
}


// ------------------------------------------------------------
// Steam Store Browse API
//
// GetItems is used to turn a plain list of appids into full game info
// (name, header image, price). Also undocumented by Valve directly —
// see xPaw's reference (xPaw, 2026b).
// ------------------------------------------------------------

interface SteamBrowseApi {

    // Takes a JSON object (as a query string, not a request body) describing
    // which appids to look up and what data to include for each one
    // (xPaw, 2026b)
    @GET("IStoreBrowseService/GetItems/v1/")
    suspend fun getItems(
        @Query("input_json") inputJson: String
    ): SteamBrowseResponse
}

data class SteamBrowseResponse(
    val response: SteamBrowseResponseData?
)

data class SteamBrowseResponseData(
    val store_items: List<SteamStoreItem>?
)

// Only the fields the app actually displays are modelled here — the real
// endpoint returns far more (xPaw, 2026b)
data class SteamStoreItem(
    val appid: Int?,
    val name: String?,
    val type: String?,
    val release: SteamRelease?,
    val basic_info: SteamBasicInfo?,
    val assets: SteamAssets?,
    val best_purchase_option: SteamPurchaseOption?
)

data class SteamRelease(
    val steam_release_date: Long?,
    val original_release_date: Long?,
    val is_coming_soon: Boolean?
)

data class SteamBasicInfo(
    val name: String?
)

// Different image sizes Steam can return for a game; the app falls back
// through these in order until it finds one that isn't null (xPaw, 2026b)
data class SteamAssets(
    val header: String?,
    val small_capsule: String?,
    val library_capsule: String?,
    val library_capsule_2x: String?
)

// Steam pre-formats the price as a display string (e.g. "$24.99"), which
// is simpler to show directly than converting cents ourselves (xPaw, 2026b)
data class SteamPurchaseOption(
    val formatted_final_price: String?,
    val final_price_in_cents: Int?,
    val currency_code: String?,
    val discount_pct: Int?
)

object SteamBrowseClient {

    val api: SteamBrowseApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamBrowseApi::class.java)
    }
}
// REFERENCE LIST

// Android Developers, 2026a. *About Credential Manager*. [online] Available at: <https://developer.android.com/guide/user-data> [Accessed 21 September 2026]
// Android Developers, 2026b. *androidx.compose.material3 | API reference*. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary> [Accessed 14 September 2026]
// Android Developers, 2026c. *BasicTextField | API reference*. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable> [Accessed 14 September 2026]
// Android Developers, 2026d. *Buttons – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/components/button> [Accessed 14 September 2026]
// Android Developers, 2026e. *Configure text fields – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/text/user-input> [Accessed 14 September 2026]
// Android Developers, 2026f. *Resources in Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/resources> [Accessed 14 September 2026]
// Android Developers, 2026g. *Work with fonts – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/text/fonts> [Accessed 14 September 2026]
// Android Developers, 2026h. *Overview of Android Custom Tabs*. [online] Available at: <https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs> [Accessed 17 September 2026]
// GeeksForGeeks, 2023. *Android Jetpack Compose – Add Data to Firebase Realtime Database*. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026]
// Google, 2026. *Firebase Documentation – Get started with Firebase Hosting*. [online] Available at: <https://firebase.google.com/docs/hosting/quickstart> [Accessed 17 September 2026]
// Gosavi, S., 2020. *Upload data in Realtime Database and retrieve it*. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026]
// Kotlin, 2026d. *Data classes*. [online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026]
// Microsoft, 2026. *Link Steam account - PlayFab | Microsoft Learn*. [online] Available at: <https://learn.microsoft.com/en-us/rest/api/playfab/client/account-management/link-steam-account?view=playfab-rest> [Accessed 16 September 2026]
// Okpukoro, T.J., 2024. *File Uploading in Android*. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026]
// Petrescu, A., 2021. *How to read data from Firebase Realtime Database using get()*. [blog] 18 February. Available at: <https://medium.com/firebase-tips-tricks/how-to-read-data-from-firebase-realtime-database-using-get-269ef3e179c5> [Accessed 1 June 2026]
// Smartherd, 2018. *Shape your Image — Circle, Rounded Square or Cuts at the Corner of Image in Android*. [blog] Available at: <https://medium.com/smartherd/shape-your-image-circle-rounded-square-or-cuts-at-the-corner-of-image-in-android-cf92f38c217f> [Accessed 20 May 2026]
// Square, 2026. *Retrofit*. [online] Available at: <https://square.github.io/retrofit/> [Accessed 16 September 2026>
// The IIE, 2026a. *Programming 3D* [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026b. *Programming 3C* [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished
// Turtle Code, 2022. *Add Photo From Gallery - Kotlin Android Tutorial*. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6-0> [Accessed 24 April 2026]
// Valve Corporation, 2026a. *Steamworks Documentation – Web Browser based authentication with OpenID*. [online] Available at: <https://partner.steamgames.com/doc/features/auth> [Accessed 20 September 2026]
// Valve Corporation, 2026b. *Steamworks Documentation – Web API Overview*. [online] Available at: <https://partner.steamgames.com/doc/webapi_overview> [Accessed 19 September 2026]
// xPaw, 2026a. *Steam Web API Documentation – IStoreBrowseService*. [online] Available at: <https://steamapi.xpaw.me/#IStoreBrowseService> [Accessed 18 September 2026]
// xPaw, 2026b. *Steam Web API Documentation – IWishlistService*. [online] Available at: <https://steamapi.xpaw.me/#IWishlistService> [Accessed 18 September 2026>