package com.example.gamelibrary

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.gamelibrary.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthHelper {

    private val auth = Firebase.auth

    private val database = Firebase.database

    // (Android Developers, 2026a)
    private val defaultScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )

    fun doGoogleSignIn(
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val credentialManager =
                    CredentialManager.create(context)

                // (Android Developers, 2026a)
                val rawNonce =
                    UUID.randomUUID().toString()

                val digest =
                    MessageDigest
                        .getInstance("SHA-256")
                        .digest(rawNonce.toByteArray())

                val hashedNonce =
                    digest.joinToString("") {
                        "%02x".format(it)
                    }

                val googleIdOption =
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(
                            BuildConfig.WEB_CLIENT_ID
                        )
                        .setAutoSelectEnabled(false)
                        .setNonce(hashedNonce)
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            googleIdOption
                        )
                        .build()

                // (Android Developers, 2026a)
                val result =
                    credentialManager.getCredential(
                        context = context,
                        request = request
                    )

                val credential =
                    result.credential

                if (
                    credential.type ==
                    GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential
                            .createFrom(
                                credential.data
                            )

                    val idToken =
                        googleCredential.idToken

                    val firebaseCredential =
                        GoogleAuthProvider
                            .getCredential(
                                idToken,
                                null
                            )

                    val authResult =
                        auth.signInWithCredential(
                            firebaseCredential
                        ).await()

                    val user =
                        authResult.user
                            ?: throw Exception(
                                "Google user could not be created"
                            )

                    onSuccess(user)

                } else {

                    onError(
                        Exception(
                            "Invalid Google credential"
                        )
                    )
                }

            } catch (e: GoogleIdTokenParsingException) {

                Log.e(
                    "GoogleAuthHelper",
                    "Google ID token parsing failed",
                    e
                )

                onError(e)

            } catch (e: GetCredentialException) {

                Log.e(
                    "GoogleAuthHelper",
                    "Google credential request failed",
                    e
                )

                onError(e)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Google sign-in failed",
                    e
                )

                onError(e)
            }
        }
    }

    // (GeeksForGeeks, 2023)
    fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val result =
                    auth.createUserWithEmailAndPassword(
                        email,
                        password
                    ).await()

                val user =
                    result.user
                        ?: throw Exception(
                            "User account could not be created"
                        )

                database
                    .reference
                    .child("users")
                    .child(user.uid)
                    .child("username")
                    .setValue(username)
                    .await()

                onSuccess(user)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Email registration failed",
                    e
                )

                onError(e)
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val result =
                    auth.signInWithEmailAndPassword(
                        email,
                        password
                    ).await()

                val user =
                    result.user
                        ?: throw Exception(
                            "User could not be signed in"
                        )

                onSuccess(user)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Email sign-in failed",
                    e
                )

                onError(e)
            }
        }
    }

    // (Petrescu, 2021)
    fun readUsername(
        uid: String,
        onResult: (String?) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val snapshot =
                    database
                        .reference
                        .child("users")
                        .child(uid)
                        .child("username")
                        .get()
                        .await()

                onResult(
                    snapshot.getValue(
                        String::class.java
                    )
                )

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Could not read username",
                    e
                )

                onError(e)
            }
        }
    }

    // (GeeksForGeeks, 2023)
    fun updateUsername(
        uid: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                database
                    .reference
                    .child("users")
                    .child(uid)
                    .child("username")
                    .setValue(username)
                    .await()

                onSuccess()

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Could not update username",
                    e
                )

                onError(e)
            }
        }
    }

    // (Okpukoro, 2024; Petrescu, 2021)
    fun readAvatarBitmap(
        uid: String,
        onResult: (Bitmap?) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val snapshot =
                    database
                        .reference
                        .child("users")
                        .child(uid)
                        .child("avatarBase64")
                        .get()
                        .await()

                val encoded =
                    snapshot.getValue(
                        String::class.java
                    )

                if (encoded.isNullOrBlank()) {

                    onResult(null)

                    return@launch
                }

                val bytes =
                    Base64.decode(
                        encoded,
                        Base64.DEFAULT
                    )

                val bitmap =
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size
                    )

                onResult(bitmap)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Could not read avatar",
                    e
                )

                onError(e)
            }
        }
    }

    // (Okpukoro, 2024; GeeksForGeeks, 2023)
    fun uploadAvatarBase64(
        uid: String,
        imageUri: Uri,
        contentResolver: ContentResolver,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {
            try {
                val encoded = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val inputStream =
                        contentResolver.openInputStream(
                            imageUri
                        )
                            ?: throw Exception(
                                "Could not open selected image"
                            )

                    val bitmap =
                        inputStream.use {
                            BitmapFactory.decodeStream(it)
                        }
                            ?: throw Exception(
                                "Could not decode selected image"
                            )

                    val resizedBitmap =
                        resizeBitmap(
                            bitmap,
                            500
                        )

                    val outputStream =
                        ByteArrayOutputStream()

                    resizedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        80,
                        outputStream
                    )

                    Base64.encodeToString(
                        outputStream.toByteArray(),
                        Base64.NO_WRAP
                    )
                }

                database
                    .reference
                    .child("users")
                    .child(uid)
                    .child("avatarBase64")
                    .setValue(encoded)
                    .await()

                onSuccess()

            } catch (e: Exception) {
                Log.e(
                    "GoogleAuthHelper",
                    "Avatar upload failed",
                    e
                )

                onError(e)
            }
        }
    }

    // (Android Developers, 2026a)
    fun requestEmailChange(
        newEmail: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val user =
                    auth.currentUser
                        ?: throw Exception(
                            "No signed-in user"
                        )

                user
                    .verifyBeforeUpdateEmail(
                        newEmail
                    )
                    .await()

                onSuccess()

            } catch (
                e: FirebaseAuthRecentLoginRequiredException
            ) {

                Log.e(
                    "GoogleAuthHelper",
                    "Recent login required for email change",
                    e
                )

                onError(e)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Email change failed",
                    e
                )

                onError(e)
            }
        }
    }

    fun deleteAccount(
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                database
                    .reference
                    .child("users")
                    .child(uid)
                    .removeValue()
                    .await()

                val user =
                    auth.currentUser
                        ?: throw Exception(
                            "No signed-in user"
                        )

                user.delete().await()

                onSuccess()

            } catch (
                e: FirebaseAuthRecentLoginRequiredException
            ) {

                Log.e(
                    "GoogleAuthHelper",
                    "Recent login required before deleting account",
                    e
                )

                onError(e)

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Account deletion failed",
                    e
                )

                onError(e)
            }
        }
    }

    fun signOut(
        onComplete: () -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                auth.signOut()

            } finally {

                onComplete()
            }
        }
    }

    // (Petrescu, 2021)
    fun readSteamId64(
        uid: String,
        onResult: (String?) -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                val snapshot =
                    database
                        .reference
                        .child("users")
                        .child(uid)
                        .child("steamId64")
                        .get()
                        .await()

                onResult(
                    snapshot.getValue(
                        String::class.java
                    )
                )

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Could not read Steam ID",
                    e
                )

                onError(e)
            }
        }
    }

    // (GeeksForGeeks, 2023; Valve Corporation, 2026a)
    fun saveSteamId64(
        uid: String,
        steamId64: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        coroutineScope: CoroutineScope = defaultScope
    ) {
        coroutineScope.launch {

            try {

                database
                    .reference
                    .child("users")
                    .child(uid)
                    .child("steamId64")
                    .setValue(steamId64)
                    .await()

                onSuccess()

            } catch (e: Exception) {

                Log.e(
                    "GoogleAuthHelper",
                    "Could not save Steam ID",
                    e
                )

                onError(e)
            }
        }
    }

    // (Okpukoro, 2024)
    private fun resizeBitmap(
        bitmap: Bitmap,
        maxSize: Int
    ): Bitmap {

        val width =
            bitmap.width.toFloat()

        val height =
            bitmap.height.toFloat()

        val scale =
            if (width > height) {
                maxSize / width
            } else {
                maxSize / height
            }

        if (scale >= 1f) {
            return bitmap
        }

        val newWidth =
            (width * scale).toInt()

        val newHeight =
            (height * scale).toInt()

        return Bitmap.createScaledBitmap(
            bitmap,
            newWidth,
            newHeight,
            true
        )
    }
}

/*
Reference List

Android Developers, 2026a. About Credential Manager. [online] Available at: <https://developer.android.com/guide/user-data> [Accessed 21 September 2026].

GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026].

Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026].

Petrescu, A., 2021. How to read data from Firebase Realtime Database using get(). [blog] 18 February. Available at: <https://medium.com/firebase-tips-tricks/how-to-read-data-from-firebase-realtime-database-using-get-269ef3e179c5> [Accessed 1 June 2026].

Valve Corporation, 2026a. Steamworks Documentation – Web Browser based authentication with OpenID. [online] Available at: <https://partner.steamgames.com/doc/features/auth> [Accessed 20 September 2026].
*/