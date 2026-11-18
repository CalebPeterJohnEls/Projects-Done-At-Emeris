package com.example.gamelibrary.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gamelibrary.AccentPurple
import com.example.gamelibrary.GoogleAuthHelper
import com.example.gamelibrary.PixelFont
import com.example.gamelibrary.PixelGradientBackground
import com.example.gamelibrary.api.WishlistItemResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private const val TAG = "WishlistScreen"

/**
 * Displays the signed-in user's real Steam wishlist, styled to match the
 * app's pixel-art gradient theme (see MainActivity.kt for
 * PixelGradientBackground / PixelFont / AccentPurple).
 *
 * Data flow: reads the linked SteamID64 from Firebase, then
 * WishlistViewModel makes the real Steam Web API calls
 * (IWishlistService/GetWishlist, then IStoreBrowseService/GetItems)
 * (xPaw, 2026a; xPaw, 2026b).
 */
@Composable
fun WishlistScreen(
    wishlistViewModel: WishlistViewModel =
        viewModel()
) {

    val authHelper = remember {
        GoogleAuthHelper()
    }

    val uid =
        Firebase.auth.currentUser?.uid

    val items by
    wishlistViewModel.items.collectAsState()

    val loading by
    wishlistViewModel.loading.collectAsState()

    val error by
    wishlistViewModel.error.collectAsState()

    LaunchedEffect(uid) {

        if (uid == null) {
            return@LaunchedEffect
        }

        authHelper.readSteamId64(

            uid = uid,

            onResult = { steamId ->

                Log.d(
                    TAG,
                    "SteamID64 found: $steamId"
                )

                if (!steamId.isNullOrBlank()) {
                    wishlistViewModel.loadWishlist(steamId)
                }
            },

            onError = {
                Log.e(TAG, "Could not read SteamID64")
            }
        )
    }

    // Same gradient/circle background used across the rest of the app,
    // instead of the plain MaterialTheme surface
    PixelGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Wishlist",
                fontFamily = PixelFont,
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            when {

                // Initial loading — spinner only
                loading && items.isEmpty() -> {
                    Spacer(Modifier.height(40.dp))
                    CircularProgressIndicator(color = Color.White)
                }

                // Error — pixel-styled message instead of MaterialTheme's
                error != null && items.isEmpty() -> {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = error ?: "Unable to load wishlist.",
                        fontFamily = PixelFont,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }

                // Empty — not linked, or linked with nothing on it
                items.isEmpty() -> {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = "Nothing here yet.",
                        fontFamily = PixelFont,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Real wishlist — pixel-card list
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = items,
                            key = { it.appId }
                        ) { item ->
                            WishlistItemCard(item)
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// White card with a black border, holding the game's header image, name,
// platform label, and price — laid out so no single text element can
// squeeze the others (see the name and price Text() modifiers below)
// (xPaw, 2026b)
@Composable
private fun WishlistItemCard(item: WishlistItemResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(2.dp, Color.Black)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = item.headerImageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 64.dp, height = 30.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(12.dp))

        // weight(1f) now safely gets "whatever space is left after the
        // fixed-width image and price column", instead of being squeezed
        // by a long price string like "Price unavailable"
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontFamily = PixelFont,
                fontSize = 11.sp,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Steam",
                fontFamily = PixelFont,
                fontSize = 8.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.width(8.dp))

        // Capped width + single line stops long strings like
        // "Price unavailable" from claiming unbounded space in the Row
        Text(
            text = item.price ?: "-",
            fontFamily = PixelFont,
            fontSize = 9.sp,
            color = AccentPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 80.dp)
        )
    }
}

// REFERENCE LIST

// Android Developers, 2026a. About Credential Manager. [online] Available at: <https://developer.android.com/guide/user-data> [Accessed 21 September 2026]
// Android Developers, 2026b. androidx.compose.material3 | API reference. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary> [Accessed 14 September 2026]
// Android Developers, 2026c. BasicTextField | API reference. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable> [Accessed 14 September 2026]
// Android Developers, 2026d. Buttons – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/components/button> [Accessed 14 September 2026]
// Android Developers, 2026e. Configure text fields – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/text/user-input> [Accessed 14 September 2026]
// Android Developers, 2026f. Resources in Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/resources> [Accessed 14 September 2026]
// Android Developers, 2026g. Work with fonts – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/text/fonts> [Accessed 14 September 2026]
// Android Developers, 2026h. Overview of Android Custom Tabs. [online] Available at: <https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs> [Accessed 17 September 2026]
// GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026]
// Google, 2026. Firebase Documentation – Get started with Firebase Hosting. [online] Available at: <https://firebase.google.com/docs/hosting/quickstart> [Accessed 17 September 2026]
// Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026]
// Kotlin, 2026d. Data classes. [online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026]
// Microsoft, 2026. Link Steam account - PlayFab | Microsoft Learn. [online] Available at: <https://learn.microsoft.com/en-us/rest/api/playfab/client/account-management/link-steam-account?view=playfab-rest> [Accessed 16 September 2026]
// Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026]
// Petrescu, A., 2021. How to read data from Firebase Realtime Database using get(). [blog] 18 February. Available at: <https://medium.com/firebase-tips-tricks/how-to-read-data-from-firebase-realtime-database-using-get-269ef3e179c5> [Accessed 1 June 2026]
// Smartherd, 2018. Shape your Image — Circle, Rounded Square or Cuts at the Corner of Image in Android. [blog] Available at: <https://medium.com/smartherd/shape-your-image-circle-rounded-square-or-cuts-at-the-corner-of-image-in-android-cf92f38c217f> [Accessed 20 May 2026]
// Square, 2026. Retrofit. [online] Available at: <https://square.github.io/retrofit/> [Accessed 16 September 2026]
// The IIE, 2026a. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished
// Turtle Code, 2022. Add Photo From Gallery - Kotlin Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6-0> [Accessed 24 April 2026]
// Valve Corporation, 2026a. Steamworks Documentation – Web Browser based authentication with OpenID. [online] Available at: <https://partner.steamgames.com/doc/features/auth> [Accessed 20 September 2026]
// Valve Corporation, 2026b. Steamworks Documentation – Web API Overview. [online] Available at: <https://partner.steamgames.com/doc/webapi_overview> [Accessed 19 September 2026]
// xPaw, 2026a. Steam Web API Documentation – IStoreBrowseService. [online] Available at: <https://steamapi.xpaw.me/#IStoreBrowseService> [Accessed 18 September 2026]
// xPaw, 2026b. Steam Web API Documentation – IWishlistService. [online] Available at: <https://steamapi.xpaw.me/#IWishlistService> [Accessed 18 September 2026]