package com.example.gamelibrary.util

import android.graphics.Bitmap
import com.example.gamelibrary.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamelibrary.BottomCard
import com.example.gamelibrary.GoogleAuthHelper
import com.example.gamelibrary.PixelFont
import com.example.gamelibrary.PixelGradientBackground
import com.example.gamelibrary.PixelPillButton
import com.example.gamelibrary.ProfileSettingScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Read-only profile view. Loads the saved username/avatar and shows them;
// editing happens on ProfileSettingScreen, reached via the Edit button
// (The IIE, 2026a; The IIE, 2026b).
@Composable
fun ProfileScreen(
    onAccountDeleted: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val authHelper = remember { GoogleAuthHelper() }
    val currentUser = Firebase.auth.currentUser

    var username by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Toggles between the view and the settings/edit form.
    var isEditing by remember { mutableStateOf(false) }

    // Reloads whenever we return from editing, so saved changes show up.
    LaunchedEffect(isEditing) {
        if (isEditing) return@LaunchedEffect

        val uid = currentUser?.uid ?: return@LaunchedEffect

        authHelper.readUsername(
            coroutineScope = coroutineScope,
            uid = uid,
            onResult = { name ->
                if (name != null) {
                    username = name
                }
            },
            onError = { }
        )

        authHelper.readAvatarBitmap(
            coroutineScope = coroutineScope,
            uid = uid,
            onResult = { bmp ->
                avatarBitmap = bmp
            },
            onError = { }
        )
    }

    if (isEditing) {
        ProfileSettingScreen(
            onBack = {
                isEditing = false
            },
            onAccountDeleted = onAccountDeleted
        )

        return
    }

    PixelGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        3.dp,
                        Color.Black,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap!!.asImageBitmap(),
                        contentDescription = "Current avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = R.drawable.default_avatar
                        ),
                        contentDescription = "Default avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            PixelPillButton(
                text = "User Profile",
                onClick = { },
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            BottomCard {
                Text(
                    text = if (username.isNotBlank()) {
                        username
                    } else {
                        "No username set"
                    },
                    color = Color.Black,
                    fontFamily = PixelFont,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = currentUser?.email ?: "No email",
                    color = Color.Gray,
                    fontFamily = PixelFont,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(20.dp))

                PixelPillButton(
                    text = "Edit Profile",
                    onClick = {
                        isEditing = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Logout button that signs the user out
                // (The IIE, 2026a; Android Developers, 2026d)
                PixelPillButton(
                    text = "logout",
                    onClick = {
                        // Signs the user out of Firebase
                        // (The IIE, 2026a)
                        authHelper.signOut(
                            coroutineScope = coroutineScope,
                            onComplete = onLogout
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}