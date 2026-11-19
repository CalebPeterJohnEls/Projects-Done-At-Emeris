package com.example.betterbudget_poe

class CategoryData {
    data class Category( //Kotlin A, 2026
        var id: Int,
        var name: String,
        //var color: String
    )
    data class Expense( //Kotlin A, 2026
        var title: String,
        var amount: Double,
        var category: Category
    )

}
/*Reference List
    W3Shools, 2026. Kotlin Syntax. [Online] Available at: <https://www.w3schools.com/kotlin/kotlin_syntax.php> [Accessed 26 April 2026]. (//w3school, 2026)

    GeeksforGeeks A, 2025. How to Create a Basic Color Picker Tool in Android? [Online] Available at: <https://www.geeksforgeeks.org/android/how-to-create-a-basic-color-picker-tool-in-android/> [Accessed 27 April 2026]. (//GeeksforGeeks A, 2025)

    Android Developers A, 2023. Use function types and lambda expressions in Kotlin. [Online] Available at: <https://developer.android.com/codelabs/basic-android-kotlin-compose-function-types-and-lambda#0> [Accessed 27 April 2026]. (//Android Developers A, 2023)

    Kotlin A, 2026. Data classes. [Online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026]. (//Kotlin A, 2026)

    Android Developers B, 2023. Use collections in Kotlin. [Online] Available at: <https://developer.android.com/codelabs/basic-android-kotlin-collections#0> [Accessed 27 April 2026].  (//Android Developers B, 2023)

    Kotlin B, 2026. AlertDialog. [Online] Available at: <https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-alert-dialog.html> [Accessed 27 April 2026].  (//Kotlin B, 2026)

    Android Developers C, 2026. Toast overview. [Online] Available at: <https://developer.android.com/guide/topics/ui/notifiers/toasts> [Accessed 27 April 2026].  (//Android Developers C, 2023)

    GeeksforGeeks B, 2025. ArrayAdapter in Android with Example. [Online] Available at: <https://www.geeksforgeeks.org/android/arrayadapter-in-android-with-example/> [Accessed 27 April 2026]. (//GeeksforGeeks B, 2025)

    Android Developers, 2025. Drawable resources. [Online] Available at: <https://developer.android.com/guide/topics/resources/drawable-resource> [Accessed 28 April 2026].  (//Android Developers, 2025)

    Android Developers D, 2026. GradientDrawable. [Online] Available at: <https://developer.android.com/reference/android/graphics/drawable/GradientDrawable> [Accessed 28 April 2026].  (//Android Developers D, 2023)*/