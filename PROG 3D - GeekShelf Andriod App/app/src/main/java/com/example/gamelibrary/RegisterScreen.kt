package com.example.gamelibrary

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser

// Register screen composable with callbacks for navigation (Bajaj, 2025; deVilla, 2023)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (user: FirebaseUser) -> Unit,
    onGoToLogin: () -> Unit,
    onBack: () -> Unit
) {
    // Gets the Android context for Toasts and the content resolver (Android Developers, 2026f)
    val context = LocalContext.current

    // Creates a coroutine scope tied to this composable (The IIE, 2026b)
    val coroutineScope = rememberCoroutineScope()

    // Remembers one helper instance across recompositions (The IIE, 2026b)
    val authHelper = remember { GoogleAuthHelper() }

    // Holds the text typed into each field (Android Developers, 2026e)
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Holds the image chosen for the avatar (Turtle Code, 2022)
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    // Opens the photo picker and stores the chosen image (Turtle Code, 2022)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    // Draws the gradient background behind the screen (Android Developers, 2026b)
    PixelGradientBackground {

        // Stacks the content vertically and makes it scrollable (Bajaj, 2025; Android Developers, 2026b)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Adds space at the top (Android Developers, 2026b)
            Spacer(Modifier.height(32.dp))

            // Shows the app logo (Android Developers, 2026f)
            GeekShelfLogo(size = 90.dp)

            // Adds space below the logo (Android Developers, 2026b)
            Spacer(Modifier.height(16.dp))

            // Button that takes the user to the login screen (Android Developers, 2026d)
            PixelPillButton(
                text = "Login",
                onClick = onGoToLogin,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            // Adds space above the form card (Android Developers, 2026b)
            Spacer(Modifier.height(24.dp))

            // White card that holds the form (Android Developers, 2026b)
            BottomCard {

                // Username input field (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    placeholder = "username"
                )

                // Space between the fields (Android Developers, 2026b)
                Spacer(Modifier.height(12.dp))

                // Email input field (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = "email"
                )

                // Space between the fields (Android Developers, 2026b)
                Spacer(Modifier.height(12.dp))

                // Password input field that hides the text (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = "password",
                    isPassword = true
                )

                // Space between the fields (Android Developers, 2026b)
                Spacer(Modifier.height(12.dp))

                // Confirm password field that hides the text (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    placeholder = "confirm password",
                    isPassword = true
                )

                // Space between the fields (Android Developers, 2026b)
                Spacer(Modifier.height(12.dp))

                // Clickable box that opens the image picker (Turtle Code, 2022)
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

                            // Launches the picker for images only (Turtle Code, 2022)
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

                    // Shows the picked file name or a prompt (Turtle Code, 2022)
                    Text(
                        text = avatarUri?.lastPathSegment
                            ?: "upload avatar...",
                        color = Color.Gray,
                        fontFamily = PixelFont,
                        fontSize = 12.sp
                    )
                }

                // Space above the buttons (Android Developers, 2026b)
                Spacer(Modifier.height(20.dp))

                // Places the register and back buttons side by side (Android Developers, 2026b)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Register button (Android Developers, 2026d)
                    PixelPillButton(
                        text = "register",
                        onClick = {

                            // Validates the input before registering (Bajaj, 2025)
                            when {

                                // Checks that email, password, and username are filled in
                                username.isBlank() ||
                                        email.isBlank() ||
                                        password.isBlank() -> {

                                    Toast.makeText(
                                        context,
                                        "Username, email and password cannot be blank",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // Checks that both passwords match (Bajaj, 2025)
                                password != confirmPassword -> {

                                    Toast.makeText(
                                        context,
                                        "Passwords do not match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {

                                    // Creates the account with Firebase
                                    authHelper.signUpWithEmail(
                                        email = email,
                                        password = password,
                                        username = username,
                                        coroutineScope = coroutineScope,

                                        onSuccess = { user ->

                                            // Copies the picked image so it can be checked for null
                                            val pickedUri = avatarUri

                                            if (pickedUri != null) {

                                                // Uploads the avatar after the account is created
                                                authHelper.uploadAvatarBase64(
                                                    uid = user.uid,
                                                    imageUri = pickedUri,
                                                    contentResolver = context.contentResolver,

                                                    onSuccess = {

                                                        Toast.makeText(
                                                            context,
                                                            "Account created and avatar uploaded",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Moves on to the home screen
                                                        onRegisterSuccess(user)
                                                    },

                                                    onError = { e ->

                                                        Toast.makeText(
                                                            context,
                                                            "Account created, but avatar upload failed: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Continues even if the avatar upload failed
                                                        onRegisterSuccess(user)
                                                    }
                                                )

                                            } else {

                                                // Confirms the account when no avatar was picked
                                                Toast.makeText(
                                                    context,
                                                    "Account created for ${user.email}",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                onRegisterSuccess(user)
                                            }
                                        },

                                        onError = { e ->

                                            // Shows the registration error
                                            Toast.makeText(
                                                context,
                                                "Unable to register: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.width(110.dp)
                    )

                    // Back button (Android Developers, 2026d)
                    PixelPillButton(
                        text = "back",
                        onClick = onBack,
                        modifier = Modifier.width(110.dp)
                    )
                }
            }

            // Adds space at the bottom (Android Developers, 2026b)
            Spacer(Modifier.height(32.dp))
        }
    }
}

/*
Reference List

Android Developers, 2026b. androidx.compose.material3 | API reference. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary> [Accessed 14 September 2026].

Android Developers, 2026c. BasicTextField | API reference. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable> [Accessed 14 September 2026].

Android Developers, 2026d. Buttons – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/components/button> [Accessed 14 September 2026].

Android Developers, 2026e. Configure text fields – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/text/user-input> [Accessed 14 September 2026].

Android Developers, 2026f. Resources in Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/resources> [Accessed 14 September 2026].

Bajaj, G., 2025. Building a Clean Login Screen in Android with Jetpack Compose. Medium, [blog] 15 October. Available at: <https://medium.com/@gauravbajaj/building-a-clean-login-screen-in-android-with-jetpack-compose-053824d05c01> [Accessed 14 September 2026].

deVilla, J., 2023. Get Started with Jetpack Compose Authentication, Part 1: Jetpack Compose Basics. Auth0 Blog, [blog] 24 February. Available at: <https://auth0.com/blog/android-authentication-jetpack-compose-part-1/> [Accessed 14 September 2026].

GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026].

Kotlin, 2026d. Data classes. [online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026].

Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026].

The IIE, 2026a. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.

The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.

Turtle Code, 2022. Add Photo From Gallery - Kotlin Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6-0> [Accessed 24 April 2026].
*/