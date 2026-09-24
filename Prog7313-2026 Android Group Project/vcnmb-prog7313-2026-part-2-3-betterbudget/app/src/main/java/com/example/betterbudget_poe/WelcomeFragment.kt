package com.example.betterbudget_poe

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<Button>(R.id.btnAdd)
        val btnSignup = view.findViewById<Button>(R.id.btnBack)

        //Navigate to Login screen
        btnLogin.setOnClickListener {
            (activity as MainActivity)
                .replaceFragment(LoginFragment())
        }

        //Navigate to Signup screen
        btnSignup.setOnClickListener {
            (activity as MainActivity)
                .replaceFragment(SignUpFragment())
        }
    }
}
//(IIE, 2026)
/*Reference list
•	Android Developers, 2026a. BaseBundle: putString. [online] Available at: <https://developer.android.com/reference/android/os/BaseBundle#putString(java.lang.String,java.lang.String)> [Accessed 20 April 2026].
•	Android Developers, 2026b. Create a Fragment. [online] Available at: <https://developer.android.com/guide/fragments/create#add-fragment-transaction> [Accessed 19 April 2026].
•	Android Developers, 2026c. Pass data between fragments. [online] Available at: <https://developer.android.com/guide/fragments/communicate#pass-data-fragment> [Accessed 18 April 2026].
•	Android Developers, 2026d. Fragment Transactions. [online] Available at: <https://developer.android.com/guide/fragments/transactions> [Accessed 19 April 2026].
•	Android Developers, 2026e. Getting a result from an Activity. [online] Available at: <https://developer.android.com/training/basics/intents/result> [Accessed 26 April 2026].
•	Android Developers, 2026f. Save key-value data with SharedPreferences. [online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 April 2026].
•	Android Developers, 2026g. TextWatcher. [online] Available at: <https://developer.android.com/reference/android/text/TextWatcher> [Accessed 25 April 2026].
•	CodingZest, 2022. Firebase Realtime Database Tutorial with Kotlin [YouTube playlist]. Available at: <https://www.youtube.com/playlist?list=PLHQRWugvckFry9Q1OT6hLNfyUizT73PwX> [Accessed 27 April 2026].
•	CodingSTUFF, 2020. Retrieve Data From Firebase Realtime Database in RecyclerView (Android Studio 2020). [video online] Available at: <https://www.youtube.com/watch?v=V4E5ROnbrGk> [Accessed 28 April 2026].
•	Firebase, 2026a. Read and Write Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/read-and-write> [Accessed 28 April 2026].
•	Firebase, 2026b. Work with Lists of Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/lists-of-data> [Accessed 18 April 2026].
•	GeeksForGeeks, 2021. Shared Preferences in Android with Examples. [online] Available at: <https://www.geeksforgeeks.org/android/shared-preferences-in-android-with-examples/> [Accessed 18 April 2026].
•	GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026].
•	GeeksForGeeks, 2025. Spinner in Kotlin. [online] Available at: <https://www.geeksforgeeks.org/kotlin/spinner-in-kotlin/> [Accessed 28 April 2026].
•	Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026].
•	The IIE. 2026. PROGRAMMING 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
•	Kotlin, 2019. Kotlin array function find. [online] Available at: <https://discuss.kotlinlang.org/t/kotlin-array-function-find/15844/4> [Accessed 18 April 2026].
•	Kotlin, 2026a. Object Declarations: Companion Objects. [online] Available at: <https://kotlinlang.org/docs/object-declarations.html#companion-objects> [Accessed 24 April 2026].
•	Kotlin, 2026b. DropDownMenuItem. [online] Available at: <https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-dropdown-menu-item.html> [Accessed 18 April 2026].
•	Kotlin, 2026c. SearchBar. [online] Available at: <https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-search-bar.html> [Accessed 20 April 2026].
•	Kotlin, 2026d. filter – Kotlin Standard Library. [online] Available at: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/filter.html> [Accessed 20 April 2026].
•	Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: https://uploadcare.com/blog/file-uploading-in-android/ [Accessed 20 April 2026].
•	Simplified Coding, 2020. Firebase Realtime Database Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=zIhfp_qftes> [Accessed 27 April 2026].
•	Stack Overflow, 2013. Fragment not attached to activity. [online] Available at: https://stackoverflow.com/questions/10919240/fragment-myfragment-not-attached-to-activity [Accessed 28 April 2026].
•	Stack Overflow, 2017. Updating the data on Firebase Android. [online] Available at: <https://stackoverflow.com/questions/41296416/updating-the-data-on-firebase-android> [Accessed 29 April 2026].
•	Stack Overflow, 2021a. How to remove an element at a specified location from a list in Kotlin. [online] Available at: <https://stackoverflow.com/questions/66531029/how-to-remove-an-element-at-a-specified-location-from-a-list-in-kotlin> [Accessed 25 April 2026].
•	Stack Overflow, 2021b. Filter a list of objects. [online] Available at: <https://stackoverflow.com/questions/70252668/filter-a-list-of-object> [Accessed 25 April 2026].
•	Turtle Code, 2022. Add Photo From Gallery - Kotlin Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6--0> [Accessed 24 April 2026].

*/