package com.example.gamelibrary

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

// Login screen composable with callbacks for navigation (Bajaj, 2025; deVilla, 2023)
@Composable
fun LoginScreen(
    onLoginSuccess: (user: FirebaseUser) -> Unit,
    onGoToRegister: () -> Unit,
    onBack: () -> Unit
) {
    // Gets the current Android context for Toast messages (Android Developers, 2026f)
    val context = LocalContext.current
    // Creates a coroutine scope tied to this composable (The IIE, 2026b)
    val coroutineScope = rememberCoroutineScope()
    // Remembers one helper instance across recompositions (The IIE, 2026b)
    val authHelper = remember { GoogleAuthHelper() }

    // Holds the email text typed by the user (Android Developers, 2026e)
    var email by remember { mutableStateOf("") }
    // Holds the password text typed by the user (Android Developers, 2026e)
    var password by remember { mutableStateOf("") }

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

            // Button that takes the user to the register screen (Android Developers, 2026d)
            PixelPillButton(
                text = "Register",
                onClick = onGoToRegister,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            // Adds space above the form card (Android Developers, 2026b)
            Spacer(Modifier.height(24.dp))

            // White card that holds the form (Android Developers, 2026b)
            BottomCard {

                // Email input field (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "email"
                )
                // Space between the fields (Android Developers, 2026b)
                Spacer(Modifier.height(14.dp))
                // Password input field that hides the text (Android Developers, 2026c; Android Developers, 2026e)
                PixelTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "password",
                    isPassword = true
                )
                // Space above the buttons (Android Developers, 2026b)
                Spacer(Modifier.height(20.dp))
                // Places the login and back buttons side by side (Android Developers, 2026b)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Login button (Android Developers, 2026d)
                    PixelPillButton(
                        text = "login",
                        onClick = {
                            // Only signs in when both fields have text (Bajaj, 2025)
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                // Signs in with email and password (The IIE, 2026a; The IIE, 2026b)
                                authHelper.signInWithEmail(
                                    coroutineScope = coroutineScope,
                                    email = email,
                                    password = password,
                                    onSuccess = { user ->
                                        // Confirms the sign-in with a Toast (Android Developers, 2026b)
                                        Toast.makeText(
                                            context,
                                            "You have signed in as ${user.email}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Moves on to the home screen (The IIE, 2026b)
                                        onLoginSuccess(user)
                                    },
                                    onError = { e ->
                                        // Shows the error message to the user (Android Developers, 2026b)
                                        Toast.makeText(
                                            context,
                                            "Unable to sign in: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                // Warns the user about blank fields (Bajaj, 2025)
                                Toast.makeText(
                                    context,
                                    "Email and password cannot be blank",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.width(110.dp)
                    )
                    // Back button (Android Developers, 2026d)
                    PixelPillButton(text = "back", onClick = onBack, modifier = Modifier.width(110.dp))
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

The IIE, 2026a. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.

The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
*/