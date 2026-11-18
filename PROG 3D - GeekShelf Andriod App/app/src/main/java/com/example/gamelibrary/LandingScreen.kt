package com.example.gamelibrary

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

// Landing screen with login, register and Google sign-in options (The IIE, 2026a; The IIE, 2026b)
@Composable
fun LandingScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGoogleSignInSuccess: (user: FirebaseUser) -> Unit
) {
    // Gets the Android context needed for Credential Manager and Toasts (The IIE, 2026a)
    val context = LocalContext.current
    // Creates a coroutine scope tied to this composable (The IIE, 2026b)
    val coroutineScope = rememberCoroutineScope()
    // Remembers one helper instance across recompositions (The IIE, 2026b)
    val authHelper = remember { GoogleAuthHelper() }

    // Draws the gradient background behind the screen (Android Developers, 2026b)
    PixelGradientBackground {
        // Stacks the content vertically and makes it scrollable (Android Developers, 2026b)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Adds space at the top (Android Developers, 2026b)
            Spacer(Modifier.height(56.dp))

            // Shows the app logo (Android Developers, 2026f)
            GeekShelfLogo()

            // Adds space below the logo (Android Developers, 2026b)
            Spacer(Modifier.height(56.dp))

            // Login button that opens the login screen (Android Developers, 2026d)
            PixelPillButton(
                text = "Login",
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth()
            )

            // Adds space between the buttons (Android Developers, 2026b)
            Spacer(Modifier.height(20.dp))

            // Register button that opens the register screen (Android Developers, 2026d)
            PixelPillButton(
                text = "Register",
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            )

            // Adds space between the buttons (Android Developers, 2026b)
            Spacer(Modifier.height(20.dp))

            // Google sign-in button (The IIE, 2026a; Android Developers, 2026d)
            PixelPillButton(
                text = "Sign in with Google",
                onClick = {
                    // (The IIE, 2026a)
                    // Starts the Google sign-in flow (The IIE, 2026a)
                    authHelper.doGoogleSignIn(
                        coroutineScope = coroutineScope,
                        context = context,
                        onSuccess = { user -> onGoogleSignInSuccess(user) },
                        onError = { e ->
                            // Shows the sign-in error to the user (The IIE, 2026a)
                            Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Adds space at the bottom (Android Developers, 2026b)
            Spacer(Modifier.height(56.dp))
        }
    }
}

/*
Reference List

Android Developers, 2026b. androidx.compose.material3 | API reference. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary> [Accessed 14 September 2026].

Android Developers, 2026d. Buttons – Jetpack Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/components/button> [Accessed 14 September 2026].

Android Developers, 2026f. Resources in Compose. [online] Available at: <https://developer.android.com/develop/ui/compose/resources> [Accessed 14 September 2026].

The IIE, 2026a. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.

The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
*/