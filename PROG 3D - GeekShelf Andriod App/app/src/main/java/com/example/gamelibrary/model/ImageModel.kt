package com.example.gamelibrary.model

// Data class holding the avatar name and Base64 image text (Kotlin, 2026d)
data class ImageModel(
    // Name of the image file (Kotlin, 2026d)
    val imageName: String? = null,
    // Image encoded as a Base64 string (Okpukoro, 2024)
    val imageBase64: String? = null
) {

    // Empty constructor required so Firebase can rebuild the object (Gosavi, 2020; GeeksForGeeks, 2023)
    constructor() : this(null, null)
}

/*
Reference List

GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026].

Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026].

Kotlin, 2026d. Data classes. [online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026].

Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026].
*/
