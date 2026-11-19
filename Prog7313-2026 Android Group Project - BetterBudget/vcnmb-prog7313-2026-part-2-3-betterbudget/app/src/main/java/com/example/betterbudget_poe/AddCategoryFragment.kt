package com.example.betterbudget_poe

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.betterbudget_poe.DataClass.Category

class AddCategoryFragment : Fragment(R.layout.fragment_add_category) {

    private lateinit var categoryRef: DatabaseReference
    private var selectedColor = "#66CDAA"

    private val colorOptions = listOf(
        "#00BCD4", "#7E57C2", "#FFC107",
        "#AB47BC", "#66CDAA", "#F44336",
        "#FF9800", "#4CAF50", "#2196F3"
    ) //GeeksforGeeks A, 2025

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref  = requireActivity() // (GeeksForGeeks, 2021; Android Developers, 2026f; Firebase 2012a)
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("username", "Guest") ?: "Guest"

        // User-scoped path: categories/{userId}  // (GeeksForGeeks, 2021; Android Developers, 2026f; Firebase 2012a)
        categoryRef = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("categories").child(currentUser)

        val btnSave  = view.findViewById<Button>(R.id.btnSave)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val colorContainer = view.findViewById<LinearLayout>(R.id.colorContainer)
        val etCategoryName = view.findViewById<EditText>(R.id.etCategoryName)

        btnBack.setOnClickListener {
            (activity as MainActivity).replaceFragment(CategoriesFragment())
        }

        // Build color swatch picker from colorOptions (//Android Developers D, 2023)

        colorContainer.removeAllViews()

        for (hex in colorOptions) {
            val swatch = View(requireContext()).apply {
                val size = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.setMargins(8, 0, 8, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    setStroke(4, Color.WHITE)
                }
                tag = hex
            }

            swatch.setOnClickListener {
                // Reset all swatches to unselected (//Android Developers D, 2023)
                for (i in 0 until colorContainer.childCount) {
                    val child    = colorContainer.getChildAt(i)
                    val childHex = child.tag as? String ?: continue
                    child.background = GradientDrawable().apply {
                        shape        = GradientDrawable.OVAL
                        setColor(Color.parseColor(childHex))
                        setStroke(4, Color.WHITE)
                    }
                }

                // Highlight selected swatch with dark border (//Android Developers D, 2023)
                swatch.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    setStroke(6, Color.parseColor("#222222"))
                }

                selectedColor = hex
                Toast.makeText(requireContext(), "Color selected", Toast.LENGTH_SHORT).show()
            }

            colorContainer.addView(swatch)
        }
        //(IIE, 2026)
        btnSave.setOnClickListener {
            val categoryName = etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                etCategoryName.error = "Enter a category name"
                return@setOnClickListener
            }
            // Check for duplicates under the user-scoped path
            // (Firebase, 2026a; Petrescu, 2021)
            categoryRef.get()
                .addOnSuccessListener { snapshot ->
                    if (!isAdded) return@addOnSuccessListener

                    val alreadyExists = snapshot.children.any { snap ->
                        snap.getValue(Category::class.java)
                            ?.name?.equals(categoryName, ignoreCase = true) == true
                    }

                    if (alreadyExists) {
                        etCategoryName.error = "'$categoryName' already exists"
                        return@addOnSuccessListener
                    }


                    val category = Category( //(IIE, 2026)
                        userId   = currentUser,
                        name     = categoryName,
                        color    = selectedColor,

                        minLimit = "0",
                        maxLimit = "0"
                    )

                    // Push creates a unique ID and saves the category
                    // (Firebase, 2026b; Gosavi, 2020)
                    categoryRef.push().setValue(category) //(Firebase, 2026b)
                        .addOnSuccessListener {
                            if (!isAdded) return@addOnSuccessListener

                            // Reset UI after successful save
                            // (Android Developers, 2026c; Stack Overflow, 2021a)
                            etCategoryName.text.clear()
                            selectedColor = "#66CDAA"

                            // Reset swatch highlights
                            for (i in 0 until colorContainer.childCount) {
                                val child    = colorContainer.getChildAt(i)
                                val childHex = child.tag as? String ?: continue
                                child.background = GradientDrawable().apply {
                                    shape    = GradientDrawable.OVAL
                                    setColor(Color.parseColor(childHex))
                                    setStroke(4, Color.WHITE)
                                }
                            }

                            Toast.makeText( //(IIE, 2026)
                                requireContext(),
                                "'$categoryName' saved!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // (Android Developers, 2026d; Stack Overflow, 2013)

                        }
                        .addOnFailureListener { e -> //(IIE, 2026)
                            if (isAdded)
                                Toast.makeText(
                                    requireContext(),
                                    "Failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                        }
                }
                .addOnFailureListener { //(IIE, 2026)
                    if (isAdded)
                        Toast.makeText(
                            requireContext(),
                            "Could not check existing categories",
                            Toast.LENGTH_SHORT
                        ).show()
                }
        }
    }
}

//(IIE, 2026)
/*Reference List
Android Developers A, 2023. Use function types and lambda expressions in Kotlin. [Online] Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-function-types-and-lambda#0 [Accessed 27 April 2026]. (//Android Developers A, 2023)
Android Developers B, 2023. Use collections in Kotlin. [Online] Available at: https://developer.android.com/codelabs/basic-android-kotlin-collections#0 [Accessed 27 April 2026]. (//Android Developers B, 2023)
Android Developers C, 2026. Toast overview. [Online] Available at: https://developer.android.com/guide/topics/ui/notifiers/toasts [Accessed 27 April 2026]. (//Android Developers C, 2023)
Android Developers D, 2026. GradientDrawable. [Online] Available at: https://developer.android.com/reference/android/graphics/drawable/GradientDrawable [Accessed 28 April 2026]. (//Android Developers D, 2023)
Android Developers E, 2026. LinearLayout. [Online] Available at: https://developer.android.com/reference/android/widget/LinearLayout [Accessed 30 May 2026]. (//Android Developers E, 2026)
Android Developers F, 2026. View. [Online] Available at: https://developer.android.com/reference/android/view/View [Accessed 30 May 2026]. (//Android Developers F, 2026)
Android Developers G, 2026. ValueEventListener. [Online] Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/database/ValueEventListener [Accessed 30 May 2026]. (//Android Developers G, 2026)
Android Developers H, 2026. DataSnapshot. [Online] Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/database/DataSnapshot [Accessed 30 May 2026]. (//Android Developers H, 2026)
Android Developers, 2025. Drawable resources. [Online] Available at: https://developer.android.com/guide/topics/resources/drawable-resource [Accessed 28 April 2026]. (//Android Developers, 2025)
Firebase, 2026a. Read and Write Data on Android. [Online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 28 April 2026].
Firebase, 2026b. Structure Your Database. [Online] Available at: https://firebase.google.com/docs/database/android/structure-data [Accessed 28 April 2026].
GeeksforGeeks A, 2025. How to Create a Basic Color Picker Tool in Android? [Online] Available at: https://www.geeksforgeeks.org/android/how-to-create-a-basic-color-picker-tool-in-android/ [Accessed 27 April 2026]. (//GeeksforGeeks A, 2025)
GeeksforGeeks B, 2025. ArrayAdapter in Android with Example. [Online] Available at: https://www.geeksforgeeks.org/android/arrayadapter-in-android-with-example/ [Accessed 27 April 2026]. (//GeeksforGeeks B, 2025)
Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html [Accessed 27 April 2026].
The IIE. 2026. PROGRAMMING 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
Kotlin A, 2026. Data classes. [Online] Available at: https://kotlinlang.org/docs/data-classes.html [Accessed 27 April 2026]. (//Kotlin A, 2026)
Kotlin B, 2026. AlertDialog. [Online] Available at: https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-alert-dialog.html [Accessed 27 April 2026]. (//Kotlin B, 2026)
Stack Overflow, 2021a. How to remove an element at a specified location from a list in Kotlin. [online] Available at: https://stackoverflow.com/questions/66531029/how-to-remove-an-element-at-a-specified-location-from-a-list-in-kotlin [Accessed 25 April 2026].
W3Schools, 2026. Kotlin Syntax. [Online] Available at: https://www.w3schools.com/kotlin/kotlin_syntax.php [Accessed 26 April 2026]. (//w3school, 2026)*/


/* Reference List
Firebase, 2026a. Read and write data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 24 May 2026].
Firebase, 2026b. Update specific fields - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write#update_specific_fields [Accessed 24 May 2026].
Firebase, 2026c. Listen for value events - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write#listen_for_value_events [Accessed 24 May 2026]..
Firebase, 2026d. Work with lists of data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/lists-of-data [Accessed 24 May 2026].
*/