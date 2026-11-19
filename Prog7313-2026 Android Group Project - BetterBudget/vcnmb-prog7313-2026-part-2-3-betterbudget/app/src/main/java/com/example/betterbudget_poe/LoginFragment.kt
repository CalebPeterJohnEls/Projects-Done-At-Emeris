package com.example.betterbudget_poe

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*


class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var userRef: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Realtime Database reference for user authentication
        // (Firebase, 2026a; Firebase, 2026b)
        userRef = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users")

        val username = view.findViewById<EditText>(R.id.tbUsername)
        val password = view.findViewById<EditText>(R.id.edtPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnAdd)
        val btnSignUp = view.findViewById<Button>(R.id.btnBack)

        // Login authentication handler
        // (Firebase, 2026a; Gosavi, 2020)
        btnLogin.setOnClickListener {

            val u = username.text.toString().trim()
            val p = password.text.toString().trim()

            // Input validation before authentication request
            // (Android Developers, 2026g; Stack Overflow, 2021b),
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sanitise username for Firebase key compatibility
            // (Firebase, 2026b; Stack Overflow, 2017)
            val safeUsername = u.replace("[.#$\\[\\]]".toRegex(), "_")

            // Retrieve user data from Firebase
            // (Firebase, 2026a; Gosavi, 2020)
            userRef.child(safeUsername).get()
                .addOnSuccessListener { snapshot ->

                    val user = snapshot.getValue(DataClass.User::class.java)

                    // Credential verification logic
                    if (user != null && user.password == p) {

                        // Store session using SharedPreferences
                        // (Android Developers, 2026f; GeeksForGeeks, 2021)
                        requireActivity()
                            .getSharedPreferences("DiaryPrefs", 0)
                            .edit()
                            .putString("username", u)
                            .apply()

                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                        // Navigate to main expense screen after login
                        // (Android Developers, 2026d; Stack Overflow, 2013)
                        if (isAdded) {
                            (activity as? MainActivity)
                                ?.replaceFragment(DashboardFragment())
                        }

                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
        }

        // Navigate back to welcome screen
        // (Android Developers, 2026d; Stack Overflow, 2013)
        btnSignUp.setOnClickListener {
            if (isAdded) {
                (activity as? MainActivity)
                    ?.replaceFragment(SignUpFragment())
            }
        }
    }
}