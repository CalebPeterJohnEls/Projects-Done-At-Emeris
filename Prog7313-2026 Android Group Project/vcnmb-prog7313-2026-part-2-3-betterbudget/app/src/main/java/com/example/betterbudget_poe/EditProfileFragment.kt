package com.example.betterbudget_poe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.util.Calendar

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                view?.findViewById<ImageView>(R.id.imagePreview)?.setImageURI(uri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.tdName)
        val etSurname = view.findViewById<EditText>(R.id.tdSurname)
        val etUsername = view.findViewById<EditText>(R.id.tdUsername)
        val datePicker = view.findViewById<DatePicker>(R.id.tdDate)
        val spinnerCurrency = view.findViewById<Spinner>(R.id.tdCurrency)
        val etEmail = view.findViewById<EditText>(R.id.tdEmail)
        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val btnAddImage = view.findViewById<Button>(R.id.btnAddImage)
        val btnSave = view.findViewById<Button>(R.id.btnSaveInfo)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // SharedPreferences used to retrieve the currently logged-in username
        // (Android Developers, 2026f; GeeksForGeeks, 2021)
        val sharedPref = requireActivity()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("username", "") ?: ""

        // Initialise Firebase Realtime Database reference
        // (Firebase, 2026a)
        val rootRef = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        val usersDb = rootRef.child("users")
        val userRef = usersDb.child(currentUser)

        // Populate currency spinner with supported currency codes
        // (GeeksForGeeks, 2025)
        val currencies = arrayOf("ZAR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD")
        spinnerCurrency.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )

        // Read existing profile data from Firebase and populate the UI fields
        // (Firebase, 2026a; Gosavi, 2020)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener

            val name = snapshot.child("name").getValue(String::class.java) ?: ""
            val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
            val username = snapshot.child("username").getValue(String::class.java) ?: currentUser
            val dob = snapshot.child("dob").getValue(String::class.java) ?: ""
            val currency = snapshot.child("currency").getValue(String::class.java) ?: "ZAR"
            val email = snapshot.child("email").getValue(String::class.java) ?: ""
            val imageUri = snapshot.child("imageUri").getValue(String::class.java) ?: ""

            etName.setText(name)
            etSurname.setText(surname)
            etUsername.setText(username)
            etEmail.setText(email)

            val currencyIndex = currencies.indexOf(currency)
            if (currencyIndex >= 0) spinnerCurrency.setSelection(currencyIndex)

            if (dob.isNotEmpty()) {
                val parts = dob.split("/")
                if (parts.size == 3) {
                    datePicker.updateDate(
                        parts[2].toInt(),
                        parts[1].toInt() - 1,
                        parts[0].toInt()
                    )
                }
            } else {
                val today = Calendar.getInstance()
                datePicker.updateDate(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                )
            }

            if (imageUri.isNotEmpty()) {
                try {
                    selectedImageUri = Uri.parse(imageUri)
                    imagePreview.setImageURI(selectedImageUri)
                } catch (e: SecurityException) {
                    imagePreview.setImageDrawable(null)
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        // Launch image picker using ActivityResultContracts to select a profile image
        // (Turtle Code, 2022; Okpukoro, 2024)
        btnAddImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Save button: validates input, reads existing data, then writes updated profile
        // (Firebase, 2026a; Stack Overflow, 2017)
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val dob = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
            val currency = spinnerCurrency.selectedItem.toString()
            val imageUri = selectedImageUri?.toString() ?: ""

            if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Read full current user data before saving so nothing is lost
            // (Firebase, 2026a; Gosavi, 2020)
            userRef.get().addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val existingPassword = snapshot.child("password").getValue(String::class.java) ?: ""

                // Build updated profile map preserving the existing password
                // (Stack Overflow, 2017)
                val updatedProfile: Map<String, Any> = mapOf(
                    "name" to name,
                    "surname" to surname,
                    "username" to username,
                    "dob" to dob,
                    "currency" to currency,
                    "email" to email,
                    "imageUri" to imageUri,
                    "password" to existingPassword
                )

                if (username != currentUser) {
                    // Username changed — must migrate ALL data to new username key.
                    // Expenses and goals are stored under the same username key in
                    // Firebase, so they must be copied to the new key and the old
                    // nodes deleted to keep all user data linked correctly.
                    // (Firebase, 2026a; Firebase, 2026b)

                    // Step 1: Read expenses under old username key
                    // (Firebase, 2026a; Gosavi, 2020)
                    rootRef.child("expenses").child(currentUser).get()
                        .addOnSuccessListener { expenseSnapshot ->

                            // Step 2: Read goals under old username key
                            // (Firebase, 2026a; Gosavi, 2020)
                            rootRef.child("goals").child(currentUser).get()
                                .addOnSuccessListener { goalsSnapshot ->
                                    if (!isAdded) return@addOnSuccessListener

                                    // Step 3: Build a single atomic multi-path update map.
                                    // HashMap<String, Any?> is required here because Firebase's
                                    // updateChildren() uses null to signal node deletion, and
                                    // a non-nullable Map<String, Any> cannot hold null values.
                                    // (Firebase, 2026a; Stack Overflow, 2017; Stack Overflow, 2018)
                                    val migrationUpdates = HashMap<String, Any?>()

                                    // Write updated profile to new username node
                                    migrationUpdates["users/$username"] = updatedProfile

                                    // Copy expenses to new username node if they exist
                                    // (Firebase, 2026a; Firebase, 2026b)
                                    val expensesValue = expenseSnapshot.value
                                    if (expensesValue != null) {
                                        migrationUpdates["expenses/$username"] = expensesValue
                                    }

                                    // Copy goals to new username node if they exist
                                    // (Firebase, 2026a; Firebase, 2026b)
                                    val goalsValue = goalsSnapshot.value
                                    if (goalsValue != null) {
                                        migrationUpdates["goals/$username"] = goalsValue
                                    }

                                    // Delete old nodes — null signals deletion in Firebase
                                    // (Firebase, 2026a; Stack Overflow, 2018)
                                    migrationUpdates["users/$currentUser"] = null
                                    migrationUpdates["expenses/$currentUser"] = null
                                    migrationUpdates["goals/$currentUser"] = null

                                    // Step 4: Execute all writes and deletes atomically
                                    // in a single updateChildren call so no data is lost
                                    // (Firebase, 2026a)
                                    rootRef.updateChildren(migrationUpdates)
                                        .addOnSuccessListener {
                                            if (isAdded) {
                                                // Update SharedPreferences to reflect new username
                                                // (Android Developers, 2026f)
                                                sharedPref.edit().putString("username", username).apply()
                                                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                                                (activity as MainActivity).replaceFragment(ProfileFragment())
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            if (isAdded) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Update failed: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    if (isAdded) {
                                        Toast.makeText(requireContext(), "Failed to read goals", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        .addOnFailureListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Failed to read expenses", Toast.LENGTH_SHORT).show()
                            }
                        }

                } else {
                    // Username unchanged — update existing node in place
                    // (Firebase, 2026a; Stack Overflow, 2017)
                    userRef.updateChildren(updatedProfile)
                        .addOnSuccessListener {
                            if (isAdded) {
                                // (Android Developers, 2026f)
                                sharedPref.edit().putString("username", username).apply()
                                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                                (activity as MainActivity).replaceFragment(ProfileFragment())
                            }
                        }
                        .addOnFailureListener { e ->
                            if (isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "Update failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }.addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to read profile, save aborted", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Cancel button navigates back to ProfileFragment without saving
        // (Android Developers, 2026d; Stack Overflow, 2013)
        btnCancel.setOnClickListener {
            activity?.let {
                (it as MainActivity).replaceFragment(ProfileFragment())
            }
        }
    }

    companion object {
        // Factory method to create a new instance with username and email arguments
        // (Kotlin, 2026a)
        fun newInstance(username: String, email: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("username", username)
                    putString("email", email)
                }
            }
    }
}


/* REFERENCES


Android Developers. (2026d). Fragment. Google.https://developer.android.com/reference/androidx/fragment/app/Fragment

Android Developers. (2026f). SharedPreferences. Google. https://developer.android.com/reference/android/content/SharedPreferences
Firebase. (2026a). Read and write data on Android. Google.https://firebase.google.com/docs/database/android/read-and-write
 Firebase. (2026b). Structure your database. Google. https://firebase.google.com/docs/database/android/structure-data
 GeeksForGeeks. (2021). Shared preferences in Android with example.https://www.geeksforgeeks.org/shared-preferences-in-android-with-example
GeeksForGeeks. (2025). Spinner in Android using Kotlin. https://www.geeksforgeeks.org/spinner-in-android-using-kotlin
 Gosavi, S. (2020). Reading data from Firebase Realtime Database in Android.
 Medium.https://medium.com/@shreegosavi/reading-data-from-firebase-realtime-database-in-android
 Kotlin. (2026a). Apply scope function. JetBrains. https://kotlinlang.org/docs/scope-functions.html#apply
 Okpukoro, E. (2024). How to pick an image from gallery in Android using Kotlin.
 Medium. https://medium.com/@okpukoro/how-to-pick-an-image-from-gallery-in-android-using-kotlin
Stack Overflow. (2013). How to go back to previous fragment on back button press.https://stackoverflow.com/questions/13418436
Stack Overflow. (2017). Firebase Android update specific fields. https://stackoverflow.com/questions/37094173
Stack Overflow. (2018). How to delete a node in Firebase Realtime Database Android.https://stackoverflow.com/questions/37094173
Turtle Code. (2022). Android image picker tutorial in Kotlin [Video]. YouTube.https://www.youtube.com/watch?v=tGMDuFgSmEY
*/