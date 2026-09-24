package com.example.betterbudget_poe

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpensesFragment : Fragment(R.layout.fragment_expenses) {

    private lateinit var expenseRef: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var currentUser: String

    // Local in-memory list used for filtering Firebase data
    // (Kotlin, 2026d; Stack Overflow, 2021b)
    private val expenseList = mutableListOf<Pair<String, DataClass.Expense>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryContainer = view.findViewById<LinearLayout>(R.id.entryContainer)
        val etSearch       = view.findViewById<EditText>(R.id.edtSearch)

        // Retrieve logged-in user session from SharedPreferences
        // (Android Developers, 2026f; GeeksForGeeks, 2021)
        val sharedPref = requireActivity().getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        currentUser    = sharedPref.getString("username", "") ?: ""

        // Firebase references
        // (Firebase, 2026a; Firebase, 2026b)
        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        expenseRef = db.child("expenses").child(currentUser)

        // Render filtered expense entries dynamically
        // (Android Developers, 2026g; Kotlin, 2026d)
        fun renderEntries(query: String) {
            if (!isAdded || context == null) return
            entryContainer.removeAllViews()

            val filtered = if (query.isEmpty()) expenseList
            else expenseList.filter { (_, expense) ->
                listOf(
                    expense.date,
                    expense.amount,
                    expense.category,
                    expense.description
                ).any { it?.lowercase()?.contains(query.lowercase()) == true }
            }

            filtered.forEach { (firebaseKey, expense) ->

                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(20, 40, 20, 40)
                }

                val imageView = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(150, 150)
                    scaleType    = ImageView.ScaleType.CENTER_CROP
                }

                // Load image URI if available
                // (Android Developers, 2026e; Okpukoro, 2024)
                if (expense.imageUri.isNotEmpty()) {
                    try {
                        imageView.setImageURI(Uri.parse(expense.imageUri))
                    } catch (e: SecurityException) {
                        imageView.setImageDrawable(null)
                    }
                }

                val textView = TextView(requireContext()).apply {
                    text = """
                        Date: ${expense.date}
                        Start Time: ${expense.startTime}
                        End Time: ${expense.endTime}
                        Amount: ${expense.amount}
                        Category: ${expense.category}
                        Description: ${expense.description}
                    """.trimIndent()
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setPadding(20, 0, 0, 0)
                }

                row.addView(imageView)
                row.addView(textView)

                // Navigate to detail view with Firebase key for update/delete operations
                // (Android Developers, 2026d; Stack Overflow, 2013)
                row.setOnClickListener {
                    (activity as MainActivity).replaceFragment(
                        DetailsExpenseFragment.newInstance(
                            date        = expense.date,
                            startTime   = expense.startTime,
                            endTime     = expense.endTime,
                            amount      = expense.amount,
                            category    = expense.category,
                            description = expense.description,
                            imageUri    = expense.imageUri,
                            firebaseKey = firebaseKey
                        )
                    )
                }

                entryContainer.addView(row)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddNew).setOnClickListener {
            (activity as MainActivity).replaceFragment(AddExpenseFragment())
        }

        // Real-time Firebase listener for automatic UI updates
        // (Firebase, 2026a; Gosavi, 2020)
        expenseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || context == null) return

                expenseList.clear()
                for (child in snapshot.children) {
                    val expense = child.getValue(DataClass.Expense::class.java)
                    if (expense != null) {
                        expenseList.add(Pair(child.key ?: "", expense))
                    }
                }

                // Recalculate score whenever expenses change (add or delete)
                AchievementsFragment.recalculateAndSaveRank(db, currentUser)

                renderEntries(etSearch.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Live search filtering using TextWatcher
        // (Android Developers, 2026g; Kotlin, 2019)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                renderEntries(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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