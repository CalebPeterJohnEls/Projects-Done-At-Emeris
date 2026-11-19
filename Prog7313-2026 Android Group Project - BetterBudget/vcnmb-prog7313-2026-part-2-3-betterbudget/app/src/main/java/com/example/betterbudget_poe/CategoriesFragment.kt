package com.example.betterbudget_poe

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.betterbudget_poe.DataClass.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.example.betterbudget_poe.AddCategoryFragment

class CategoriesFragment : Fragment() {

    private lateinit var db: DatabaseReference
    private lateinit var userId: String
    private lateinit var llCategoriesContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance( // (GeeksForGeeks, 2021; Android Developers, 2026f; Firebase 2012a)
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        userId = requireContext() // (GeeksForGeeks, 2021; Android Developers, 2026f)
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: ""

        llCategoriesContainer = view.findViewById(R.id.llCategoriesContainer)

        view.findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            (activity as MainActivity).replaceFragment(AddCategoryFragment())
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }
    //(IIE, 2026)
    private fun loadCategories() {
        db.child("categories").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                llCategoriesContainer.removeAllViews()

                if (!snapshot.exists()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }

                for (snap in snapshot.children) {
                    val category = snap.getValue(Category::class.java) ?: continue
                    val snapKey  = snap.key ?: continue
                    addCategoryView(snapKey, category)
                }
            }
            .addOnFailureListener {
                if (isAdded)
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    //(IIE, 2026)
    private fun addCategoryView(snapKey: String, category: Category) {
        if (!isAdded) return

        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_category, llCategoriesContainer, false)

        val viewColorSwatch = itemView.findViewById<View>(R.id.viewColorSwatch)
        val tvCategoryName = itemView.findViewById<TextView>(R.id.tvCategoryName)
        val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDeleteCategory)

        tvCategoryName.text = category.name

        // Draw the color swatch as a filled circle matching the category color (//Android Developers D, 2023)
        viewColorSwatch.background = GradientDrawable().apply {
            shape        = GradientDrawable.OVAL
            setColor(parseColorSafe(category.color))
            setStroke(3, Color.parseColor("#DDDDDD"))
        }


//(IIE, 2026)
        //Android Developers C, (2023); //Stack Overflow, 2021a
        btnDelete.setOnClickListener {
            db.child("categories").child(userId).child(snapKey).removeValue()
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "'${category.name}' deleted", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    }
                }
                .addOnFailureListener {
                    if (isAdded)
                        Toast.makeText(requireContext(), "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        llCategoriesContainer.addView(itemView)
    }

    // Safely parses a hex color string, falls back to teal if invalid //GeeksforGeeks A, 2025 //(IIE, 2026)
    private fun parseColorSafe(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.parseColor("#66CDAA")
        }
    }
    //(IIE, 2026)
    private fun showEmptyState() {
        if (!isAdded) return
        val emptyView = TextView(context).apply {
            text     = "No categories yet. Tap + to add one."
            textSize = 16f
            gravity  = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            setTextColor(Color.WHITE)
        }
        llCategoriesContainer.addView(emptyView)
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