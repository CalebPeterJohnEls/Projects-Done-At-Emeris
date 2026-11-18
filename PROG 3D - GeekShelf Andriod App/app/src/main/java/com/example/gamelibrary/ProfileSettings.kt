package com.example.gamelibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileSettingScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authHelper = remember { GoogleAuthHelper() }
    val currentUser = Firebase.auth.currentUser

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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
            onResult = { bitmap ->
                avatarBitmap = bitmap
            },
            onError = { }
        )
    }

    LaunchedEffect(avatarUri) {
        val uri = avatarUri

        previewBitmap = if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            null
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        avatarUri = uri
    }

    if (showDeleteConfirm) {
        DeleteAccountConfirmScreen(
            onCancel = {
                showDeleteConfirm = false
            },
            onConfirm = {
                val uid = currentUser?.uid

                if (uid == null) {
                    Toast.makeText(
                        context,
                        "No signed-in user",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@DeleteAccountConfirmScreen
                }

                authHelper.deleteAccount(
                    uid = uid,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Account deleted",
                            Toast.LENGTH_SHORT
                        ).show()

                        onAccountDeleted()
                    },
                    onError = { e ->
                        Toast.makeText(
                            context,
                            "Unable to delete account: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
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
                        width = 3.dp,
                        color = Color.Black,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap = previewBitmap ?: avatarBitmap

                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
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
                text = "Edit Profile",
                onClick = { },
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            BottomCard {
                PixelTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    placeholder = "username"
                )

                Spacer(Modifier.height(12.dp))

                PixelTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = "email"
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = Color.Black
                        )
                        .background(Color.White)
                        .padding(
                            horizontal = 14.dp,
                            vertical = 14.dp
                        )
                        .clickable {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia
                                        .ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarUri?.lastPathSegment
                            ?: "change avatar...",
                        color = Color.Gray,
                        fontFamily = PixelFont,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                PixelPillButton(
                    text = "Delete Account",
                    onClick = {
                        showDeleteConfirm = true
                    },
                    containerColor = Color(0xFFB33A3A),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PixelPillButton(
                        text = "accept",
                        onClick = {
                            val uid = currentUser?.uid

                            if (uid == null) {
                                Toast.makeText(
                                    context,
                                    "No signed-in user",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@PixelPillButton
                            }

                            val pickedUri = avatarUri

                            val emailChanged =
                                email.isNotBlank() &&
                                        email != currentUser.email

                            fun finish() {
                                Toast.makeText(
                                    context,
                                    "Profile updated",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onBack()
                            }

                            fun saveEmail() {
                                if (emailChanged) {
                                    authHelper.requestEmailChange(
                                        newEmail = email,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Check $email to confirm your new address",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            finish()
                                        },
                                        onError = { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update email: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            finish()
                                        }
                                    )
                                } else {
                                    finish()
                                }
                            }

                            fun saveUsername() {
                                authHelper.updateUsername(
                                    uid = uid,
                                    username = username,
                                    onSuccess = {
                                        saveEmail()
                                    },
                                    onError = { e ->
                                        Toast.makeText(
                                            context,
                                            "Failed to save username: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        saveEmail()
                                    }
                                )
                            }

                            fun saveAvatar() {
                                if (pickedUri != null) {
                                    authHelper.uploadAvatarBase64(
                                        uid = uid,
                                        imageUri = pickedUri,
                                        contentResolver = context.contentResolver,
                                        onSuccess = {
                                            saveUsername()
                                        },
                                        onError = { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to save avatar: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            saveUsername()
                                        }
                                    )
                                } else {
                                    saveUsername()
                                }
                            }

                            saveAvatar()
                        },
                        modifier = Modifier.width(110.dp)
                    )

                    PixelPillButton(
                        text = "cancel",
                        onClick = onBack,
                        modifier = Modifier.width(110.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeleteAccountConfirmScreen(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    PixelGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(32.dp))

            GeekShelfLogo(size = 120.dp)

            Spacer(Modifier.height(16.dp))

            PixelPillButton(
                text = "User Profile",
                onClick = { },
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            BottomCard {
                Text(
                    text = "Are you sure you want to delete your account? " +
                            "(Even Shenron cannot undo this :( )",
                    color = Color.Black,
                    fontFamily = PixelFont,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PixelPillButton(
                        text = "accept",
                        onClick = onConfirm,
                        modifier = Modifier.width(110.dp)
                    )

                    PixelPillButton(
                        text = "cancel",
                        onClick = onCancel,
                        modifier = Modifier.width(110.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}