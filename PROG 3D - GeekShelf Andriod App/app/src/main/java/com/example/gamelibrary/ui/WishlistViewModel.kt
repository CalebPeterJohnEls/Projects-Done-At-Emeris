package com.example.gamelibrary.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamelibrary.api.SteamBrowseClient
import com.example.gamelibrary.api.SteamStoreClient
import com.example.gamelibrary.api.WishlistItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Loads the signed-in user's real Steam wishlist in two steps: first get
 * the list of appids (IWishlistService/GetWishlist), then look up full
 * details for those appids (IStoreBrowseService/GetItems) — Steam's
 * wishlist endpoint only returns bare appids, not names or prices
 * (xPaw, 2026a; xPaw, 2026b).
 */
class WishlistViewModel : ViewModel() {

    private val _items =
        MutableStateFlow<List<WishlistItemResponse>>(emptyList())

    val items: StateFlow<List<WishlistItemResponse>> =
        _items.asStateFlow()


    private val _loading =
        MutableStateFlow(false)

    val loading: StateFlow<Boolean> =
        _loading.asStateFlow()


    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()


    // Remembers which Steam account was last loaded, so switching bottom
    // nav tabs doesn't trigger a pointless reload of the same data
    private var loadedSteamId: String? = null


    // ------------------------------------------------------------
    // Load Wishlist
    // ------------------------------------------------------------

    fun loadWishlist(steamId: String) {

        // Skip the network calls entirely if this account's data is
        // already sitting in _items
        if (loadedSteamId == steamId && _items.value.isNotEmpty()) {
            Log.d(
                "WishlistViewModel",
                "Wishlist already loaded for SteamID: $steamId"
            )
            return
        }

        viewModelScope.launch {

            try {

                _loading.value = true
                _error.value = null

                Log.d(
                    "WishlistViewModel",
                    "Loading Steam wishlist for: $steamId"
                )


                // ------------------------------------------------
                // STEP 1
                // Get the real Steam wishlist (xPaw, 2026a)
                // ------------------------------------------------

                val wishlistResponse =
                    SteamStoreClient.api.getWishlist(steamId)

                val wishlistEntries =
                    wishlistResponse.response?.items.orEmpty()

                Log.d(
                    "WishlistViewModel",
                    "Steam returned ${wishlistEntries.size} wishlist entries"
                )

                if (wishlistEntries.isEmpty()) {

                    _items.value = emptyList()
                    loadedSteamId = steamId

                    Log.d(
                        "WishlistViewModel",
                        "Steam wishlist is empty"
                    )

                    return@launch
                }


                // ------------------------------------------------
                // STEP 2
                // Build the request body GetItems expects: a JSON object
                // listing which appids to look up, and which fields to
                // include for each one (xPaw, 2026b)
                // ------------------------------------------------

                val ids = JSONArray()

                wishlistEntries.forEach { entry ->

                    val item = JSONObject()

                    item.put(
                        "appid",
                        entry.appid
                    )

                    ids.put(item)
                }


                // Tells Steam which language/region/context to format
                // results for
                val context = JSONObject()

                context.put(
                    "language",
                    "english"
                )

                context.put(
                    "country_code",
                    "ZA"
                )

                context.put(
                    "steam_realm",
                    1
                )


                // Flags for which optional fields Steam should include
                // in its response for each game
                val dataRequest = JSONObject()

                dataRequest.put(
                    "include_assets",
                    true
                )

                dataRequest.put(
                    "include_release",
                    true
                )

                dataRequest.put(
                    "include_basic_info",
                    true
                )

                dataRequest.put(
                    "include_best_purchase_option",
                    true
                )


                val request = JSONObject()

                request.put(
                    "ids",
                    ids
                )

                request.put(
                    "context",
                    context
                )

                request.put(
                    "data_request",
                    dataRequest
                )


                // Retrofit encodes @Query values itself, so the JSON must
                // be passed as a plain string here — encoding it first
                // would double-encode it and cause an HTTP 400 (Square, 2026)
                val json = request.toString()

                Log.d(
                    "WishlistViewModel",
                    "GetItems JSON: $json"
                )

                Log.d(
                    "WishlistViewModel",
                    "Requesting Steam store information..."
                )


                // ------------------------------------------------
                // STEP 3
                // Ask Steam for full game info in one batched request
                // instead of one request per game (xPaw, 2026b)
                // ------------------------------------------------

                val storeResponse =
                    SteamBrowseClient.api.getItems(
                        inputJson = json
                    )


                val storeItems =
                    storeResponse.response?.store_items.orEmpty()

                Log.d(
                    "WishlistViewModel",
                    "GetItems returned ${storeItems.size} items"
                )


                // ------------------------------------------------
                // STEP 4
                // Index the store results by appid so each wishlist
                // entry can be matched to its details in step 5
                // ------------------------------------------------

                val storeItemsByAppId =
                    storeItems
                        .filter { it.appid != null }
                        .associateBy {
                            it.appid!!
                        }


                // ------------------------------------------------
                // STEP 5
                // Combine the wishlist order with the store details into
                // the WishlistItemResponse objects the UI displays
                // ------------------------------------------------

                val games =
                    wishlistEntries.mapNotNull { wishlistEntry ->

                        val appId =
                            wishlistEntry.appid

                        val storeItem =
                            storeItemsByAppId[appId]


                        // Skip any game Steam didn't return details for
                        if (storeItem == null) {

                            Log.w(
                                "WishlistViewModel",
                                "No store information for AppID: $appId"
                            )

                            return@mapNotNull null
                        }


                        // Falls back to basic_info.name if the top-level
                        // name field is missing
                        val name =
                            storeItem.name
                                ?: storeItem.basic_info?.name
                                ?: "Unknown Game"


                        // Tries each image size in order until one exists,
                        // and falls back to Steam's CDN convention if none do
                        val headerImage =
                            storeItem.assets?.header
                                ?: storeItem.assets?.small_capsule
                                ?: storeItem.assets?.library_capsule
                                ?: storeItem.assets?.library_capsule_2x
                                ?: "https://cdn.cloudflare.steamstatic.com/steam/apps/$appId/header.jpg"


                        // Steam already formats this as a display string
                        // (e.g. "$24.99"), so no manual cents conversion needed
                        val price =
                            storeItem.best_purchase_option
                                ?.formatted_final_price
                                ?: "Price unavailable"


                        Log.d(
                            "WishlistViewModel",
                            "Loaded REAL Steam game: $name ($appId)"
                        )


                        WishlistItemResponse(
                            appId = appId,
                            name = name,
                            headerImageUrl = headerImage,
                            price = price
                        )
                    }


                // ------------------------------------------------
                // STEP 6
                // Publish the finished list and remember which account
                // it belongs to
                // ------------------------------------------------

                _items.value = games

                loadedSteamId = steamId

                Log.d(
                    "WishlistViewModel",
                    "Final wishlist size: ${games.size}"
                )

            } catch (e: HttpException) {

                // Retrofit throws this specifically for non-2xx HTTP responses
                val errorMessage =
                    "Steam HTTP error: ${e.code()}"

                Log.e(
                    "WishlistViewModel",
                    errorMessage,
                    e
                )

                _error.value = errorMessage

            } catch (e: Exception) {

                Log.e(
                    "WishlistViewModel",
                    "Failed to load Steam wishlist",
                    e
                )

                _error.value =
                    e.message ?: "Failed to load Steam wishlist"

            } finally {

                // Runs whether the load succeeded or failed, so the
                // loading spinner never gets stuck on screen
                _loading.value = false
            }
        }
    }


    // ------------------------------------------------------------
    // Force refresh
    // ------------------------------------------------------------

    // Clears the "already loaded" guard so loadWishlist runs again
    // even for the same Steam account
    fun refreshWishlist(steamId: String) {

        Log.d(
            "WishlistViewModel",
            "Refreshing Steam wishlist"
        )

        loadedSteamId = null

        _items.value = emptyList()

        loadWishlist(steamId)
    }


    // ------------------------------------------------------------
    // Clear wishlist cache
    // ------------------------------------------------------------

    // Used when the user unlinks Steam or signs out, so stale wishlist
    // data doesn't briefly appear for the next user
    fun clearWishlist() {

        loadedSteamId = null

        _items.value = emptyList()

        _error.value = null

        Log.d(
            "WishlistViewModel",
            "Wishlist cache cleared"
        )
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