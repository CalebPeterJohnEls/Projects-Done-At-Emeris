package com.example.betterbudget_poe

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class EditExpenseFragment : Fragment(R.layout.fragment_edit_expense) {

    private var selectedImageUri: Uri? = null
    private var selectedCategory: String = ""
    private lateinit var categoryRef: DatabaseReference

    // Image picker using Activity Result API
    // (Android Developers, 2026e; Okpukoro, 2024)
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                view?.findViewById<ImageView>(R.id.imagePreview)?.setImageURI(uri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragments are used to build reusable UI components in Android (Android Developers, 2026b)
        val args = requireArguments()

        val firebaseKey = args.getString("firebaseKey", "")
        val existingUri = args.getString("imageUri", "")

        // Firebase Realtime Database reference setup
        // (Firebase, 2026a; Firebase, 2026b)
        val database = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
        categoryRef = database.getReference("categories")

        // SharedPreferences is used to store small key-value data like logged-in user info
        // (GeeksForGeeks, 2021; Android Developers, 2026f)
        val sharedPref = requireActivity().getSharedPreferences("DiaryPrefs", 0)
        val currentUser = sharedPref.getString("username", "") ?: ""

        val datePicker = view.findViewById<DatePicker>(R.id.tdDate)
        val tpStart = view.findViewById<TimePicker>(R.id.tdStartTime)
        val tpEnd = view.findViewById<TimePicker>(R.id.tdEndTime)
        val etAmount = view.findViewById<EditText>(R.id.tdAmount)
        val etDescription = view.findViewById<EditText>(R.id.tdDescription)
        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSaveExpence)
        val btnAddImage = view.findViewById<Button>(R.id.btnAddImage)
        val categorySpinner = view.findViewById<Spinner>(R.id.tdCategory)

        // DatePicker initialised to existing date or today's date
        // (GeeksForGeeks, 2023; Android Developers, 2026b)
        val existingDate = args.getString("date", "")
        if (existingDate.isNotEmpty()) {
            val parts = existingDate.split("/")
            if (parts.size == 3) {
                datePicker.updateDate(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        } else {
            val today = Calendar.getInstance()
            datePicker.updateDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        }

        // Parse saved time string (format: hh:mm AM/PM) back into TimePicker
        fun parseTimeIntoTimePicker(timeStr: String, picker: TimePicker) {
            if (timeStr.isEmpty()) return
            val parts = timeStr.split(":", " ")
            if (parts.size == 3) {
                var hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val amPm = parts[2]
                if (amPm == "AM" && hour == 12) hour = 0
                else if (amPm == "PM" && hour != 12) hour += 12
                picker.hour = hour
                picker.minute = minute
            }
        }

        parseTimeIntoTimePicker(args.getString("startTime", ""), tpStart)
        parseTimeIntoTimePicker(args.getString("endTime", ""), tpEnd)

        // Input validation before populating fields
        // (Android Developers, 2026g; Stack Overflow, 2021b)
        val amount = args.getString("amount", "")
        if (amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            etAmount.error = "Enter a valid amount"
        } else {
            etAmount.setText(amount)
        }

        etDescription.setText(args.getString("description", ""))

        if (existingUri.isNotEmpty()) {
            selectedImageUri = Uri.parse(existingUri)
            imagePreview.setImageURI(selectedImageUri)
        }

        // Load categories dynamically from Firebase into the Spinner
        // so it stays in sync with whatever the user has added in CategoriesFragment,
        // rather than using a hardcoded array
        // (Firebase, 2026a; Firebase, 2026b; Skelton, 2017; GeeksForGeeks, 2025)
        loadCategoriesIntoSpinner(categorySpinner, currentUser, btnSave, args.getString("category", ""))

        // Adds image
        btnAddImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnCancel.setOnClickListener {
            activity?.let {
                (it as MainActivity).replaceFragment(ExpensesFragment())
            }
        }

        // Saves updated information to Firebase database
        btnSave.setOnClickListener {

            val updatedAmount = etAmount.text.toString().trim()
            val updatedDescription = etDescription.text.toString().trim()
            val date = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
            val startTime = formatTime(tpStart)
            val endTime = formatTime(tpEnd)
            val imageUri = selectedImageUri?.toString() ?: ""

            // Input validation before database submission
            // (Android Developers, 2026g; Stack Overflow, 2021b)
            if (updatedAmount.toDoubleOrNull() == null || updatedAmount.toDouble() <= 0) {
                etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            if (updatedDescription.isEmpty()) {
                etDescription.error = "Enter a description"
                return@setOnClickListener
            }

            if (selectedCategory.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase updateChildren() is used to update only specific fields instead of replacing whole record
            // (Stack Overflow, 2017)
            val updates: Map<String, Any> = mapOf(
                "date" to date,
                "startTime" to startTime,
                "endTime" to endTime,
                "amount" to updatedAmount,
                "description" to updatedDescription,
                "category" to selectedCategory,
                "imageUri" to imageUri
            )

            database.getReference("expenses")
                .child(currentUser)
                .child(firebaseKey)
                .updateChildren(updates)
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Entry updated!", Toast.LENGTH_SHORT).show()
                        activity?.let {
                            (it as MainActivity).replaceFragment(ExpensesFragment())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun loadCategoriesIntoSpinner(
        spinner: Spinner,
        currentUser: String,
        btnSave: Button,
        existingCategory: String
    ) {
        // One-time read using .get() navigating to the current user's categories node.
        // .get() is used instead of a persistent listener because the data only needs
        // to be fetched once when the screen opens, not continuously monitored
        // (Firebase, 2026a; Petrescu, 2021)
        categoryRef.child(currentUser).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                // First entry acts as a prompt, not a selectable value
                // (GeeksForGeeks, 2025; Android Developers, 2026b)
                val categoryNames = mutableListOf("Select a category")

                // Iterate over each child node in the snapshot and map it to
                // the Category data class using getValue() — the same pattern
                // used in CategoriesFragment.loadCategories()
                // (Firebase, 2026b; Skelton, 2017)
                for (snap in snapshot.children) {
                    val category = snap.getValue(DataClass.Category::class.java) ?: continue
                    categoryNames.add(category.name)
                }

                // Always have "Other" at the bottom
                // (Kotlin, 2019)
                categoryNames.add("Other")

                // If the only items are the prompt and "Other", no real categories exist.
                // Warn the user and disable save so they cannot submit without a valid category.
                // size == 2 means only "Select a category" and "Other" are present
                // (Android Developers, 2026b; Android Developers, 2026g)
                if (categoryNames.size == 2) {
                    Toast.makeText(
                        requireContext(),
                        "No categories found. Please go to Categories and add one first.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Disable save button to block submission without a real category
                    // (Android Developers, 2026b)
                    btnSave.isEnabled = false

                    // Still bind the adapter so the spinner renders cleanly
                    // (GeeksForGeeks, 2025)
                    spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    )
                    return@addOnSuccessListener
                }

                // Categories exist — make sure save is enabled in case the user
                // navigated away to add one and came back
                // (Android Developers, 2026b)
                btnSave.isEnabled = true

                // ArrayAdapter binds the populated list to the Spinner UI component
                // (Android Developers, 2026b; GeeksForGeeks, 2025)
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )

                // Sets the layout used for the expanded dropdown rows
                // (GeeksForGeeks, 2025)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // Pre-select the existing category if it exists in the list
                // (Kotlin, 2019)
                val existingIndex = categoryNames.indexOf(existingCategory)
                if (existingIndex >= 0) {
                    spinner.setSelection(existingIndex, false)
                    selectedCategory = existingCategory
                } else {
                    // setSelection(0, false) placed before onItemSelectedListener
                    // to prevent the listener firing automatically on fragment load
                    // (GeeksForGeeks, 2025; Android Developers, 2026b)
                    spinner.setSelection(0, false)
                }

                // Handling spinner item selection events
                // (Android Developers, 2026b; Kotlin, 2019)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        // Position 0 is the "Select a category" prompt, not a real value
                        // (GeeksForGeeks, 2025)
                        selectedCategory = if (position == 0) "" else categoryNames[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedCategory = ""
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded)
                    Toast.makeText(
                        requireContext(),
                        "Failed to load categories: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
            }
    }

    // Formats a TimePicker value to a 12-hour AM/PM string
    private fun formatTime(timePicker: TimePicker): String {
        var hour = timePicker.hour
        val minute = timePicker.minute
        val amPm: String

        when {
            hour == 0  -> { hour += 12; amPm = "AM" }
            hour == 12 -> amPm = "PM"
            hour > 12  -> { hour -= 12; amPm = "PM" }
            else       -> amPm = "AM"
        }

        val h = if (hour < 10) "0$hour" else "$hour"
        val m = if (minute < 10) "0$minute" else "$minute"
        return "$h:$m $amPm"
    }

    companion object {

        fun newInstance(
            firebaseKey: String,
            date: String,
            startTime: String,
            endTime: String,
            amount: String,
            category: String,
            description: String,
            imageUri: String
        ): EditExpenseFragment {

            return EditExpenseFragment().apply {
                arguments = Bundle().apply {
                    putString("firebaseKey", firebaseKey)
                    putString("date", date)
                    putString("startTime", startTime)
                    putString("endTime", endTime)
                    putString("amount", amount)
                    putString("category", category)
                    putString("description", description)
                    putString("imageUri", imageUri)
                }
            }
        }
    }
}
//(IIE, 2026)
/*Reference list
- Android Developers, 2026a. BaseBundle: putString. [online] Available at: <https://developer.android.com/reference/android/os/BaseBundle#putString(java.lang.String,java.lang.String)> [Accessed 20 April 2026].
- Android Developers, 2026b. Create a Fragment. [online] Available at: <https://developer.android.com/guide/fragments/create#add-fragment-transaction> [Accessed 19 April 2026].
- Android Developers, 2026c. Pass data between fragments. [online] Available at: <https://developer.android.com/guide/fragments/communicate#pass-data-fragment> [Accessed 18 April 2026].
- Android Developers, 2026d. Fragment Transactions. [online] Available at: <https://developer.android.com/guide/fragments/transactions> [Accessed 19 April 2026].
- Android Developers, 2026e. Getting a result from an Activity. [online] Available at: <https://developer.android.com/training/basics/intents/result> [Accessed 26 April 2026].
- Android Developers, 2026f. Save key-value data with SharedPreferences. [online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 April 2026].
- Android Developers, 2026g. TextWatcher. [online] Available at: <https://developer.android.com/reference/android/text/TextWatcher> [Accessed 25 April 2026].
- CodingZest, 2022. Firebase Realtime Database Tutorial with Kotlin [YouTube playlist]. Available at: <https://www.youtube.com/playlist?list=PLHQRWugvckFry9Q1OT6hLNfyUizT73PwX> [Accessed 27 April 2026].
- CodingSTUFF, 2020. Retrieve Data From Firebase Realtime Database in RecyclerView (Android Studio 2020). [video online] Available at: <https://www.youtube.com/watch?v=V4E5ROnbrGk> [Accessed 28 April 2026].
- Firebase, 2026a. Read and Write Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/read-and-write> [Accessed 28 April 2026].
- Firebase, 2026b. Work with Lists of Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/lists-of-data> [Accessed 18 April 2026].
- GeeksForGeeks, 2021. Shared Preferences in Android with Examples. [online] Available at: <https://www.geeksforgeeks.org/android/shared-preferences-in-android-with-examples/> [Accessed 18 April 2026].
- GeeksForGeeks, 2023. Android Jetpack Compose – Add Data to Firebase Realtime Database. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026].
- GeeksForGeeks, 2025. Spinner in Kotlin. [online] Available at: <https://www.geeksforgeeks.org/kotlin/spinner-in-kotlin/> [Accessed 28 April 2026].
- Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026].
- The IIE. 2026. PROGRAMMING 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
- Kotlin, 2019. Kotlin array function find. [online] Available at: <https://discuss.kotlinlang.org/t/kotlin-array-function-find/15844/4> [Accessed 18 April 2026].
- Kotlin, 2026a. Object Declarations: Companion Objects. [online] Available at: <https://kotlinlang.org/docs/object-declarations.html#companion-objects> [Accessed 24 April 2026].
- Kotlin, 2026b. DropDownMenuItem. [online] Available at: <https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-dropdown-menu-item.html> [Accessed 18 April 2026].
- Kotlin, 2026c. SearchBar. [online] Available at: <https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-search-bar.html> [Accessed 20 April 2026].
- Kotlin, 2026d. filter – Kotlin Standard Library. [online] Available at: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/filter.html> [Accessed 20 April 2026].
- Okpukoro, T.J., 2024. File Uploading in Android. [blog] 23 July. Available at: https://uploadcare.com/blog/file-uploading-in-android/ [Accessed 20 April 2026].
- Petrescu, A., 2021. How to read data from Firebase Realtime Database using get(). [blog] 18 February. Available at: <https://medium.com/firebase-tips-tricks/how-to-read-data-from-firebase-realtime-database-using-get-269ef3e179c5> [Accessed 1 June 2026].
- Simplified Coding, 2020. Firebase Realtime Database Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=zIhfp_qftes> [Accessed 27 April 2026].
- Skelton, N., 2017. Storing and retrieving data from Firebase with Kotlin on Android. [blog] 14 July. Available at: <https://medium.com/a-practical-guide-to-firebase-on-android/storing-and-retrieving-data-from-firebase-with-kotlin-on-android-91c36680771> [Accessed 1 June 2026].
- Stack Overflow, 2013. Fragment not attached to activity. [online] Available at: https://stackoverflow.com/questions/10919240/fragment-myfragment-not-attached-to-activity [Accessed 28 April 2026].
- Stack Overflow, 2017. Updating the data on Firebase Android. [online] Available at: <https://stackoverflow.com/questions/41296416/updating-the-data-on-firebase-android> [Accessed 29 April 2026].
- Stack Overflow, 2021a. How to remove an element at a specified location from a list in Kotlin. [online] Available at: <https://stackoverflow.com/questions/66531029/how-to-remove-an-element-at-a-specified-location-from-a-list-in-kotlin> [Accessed 25 April 2026].
- Stack Overflow, 2021b. Filter a list of objects. [online] Available at: <https://stackoverflow.com/questions/70252668/filter-a-list-of-object> [Accessed 25 April 2026].
- Turtle Code, 2022. Add Photo From Gallery - Kotlin Android Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6--0> [Accessed 24 April 2026].
*/