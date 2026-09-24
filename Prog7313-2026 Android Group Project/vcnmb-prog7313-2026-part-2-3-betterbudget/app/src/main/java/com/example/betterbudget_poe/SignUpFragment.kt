package com.example.betterbudget_poe

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private lateinit var userRef: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Realtime Database reference for user storage
        // (Firebase, 2026a; Firebase, 2026b)
        userRef = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users")

        val username = view.findViewById<EditText>(R.id.tbUsername)
        val email = view.findViewById<EditText>(R.id.edtMinLimit)
        val password = view.findViewById<EditText>(R.id.edtGoalDate)
        val confirm = view.findViewById<EditText>(R.id.edtConfirmPassword)
        val btnSignup = view.findViewById<Button>(R.id.btnBack)
        val btnLogin = view.findViewById<Button>(R.id.btnAdd)

        //Login button event handler
        btnLogin.setOnClickListener {
            if (isAdded) {
                (activity as MainActivity)
                    .replaceFragment(LoginFragment())
            }

        }

        // Sign-up button event handler
        // (Android Developers, 2026g; Stack Overflow, 2021b)
        btnSignup.setOnClickListener {

            val u = username.text.toString().trim()
            val e = email.text.toString().trim()
            val p = password.text.toString().trim()
            val cp = confirm.text.toString().trim()

            // Input validation before database write
            // (Android Developers, 2026g; GeeksForGeeks, 2021)
            if (u.isEmpty() || e.isEmpty() || p.isEmpty() || cp.isEmpty()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password confirmation validation
            if (p != cp) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sanitise username for Firebase key compatibility
            // (Firebase, 2026b; Stack Overflow, 2017)
            val safeUsername = u.replace("[.#$\\[\\]]".toRegex(), "_")

            val user = DataClass.User(u, e, p)

            // Firebase write operation (create account)
            // (Firebase, 2026a; Gosavi, 2020)
            userRef.child(safeUsername).setValue(user)
                .addOnSuccessListener {

                    Toast.makeText(context, "Account created", Toast.LENGTH_SHORT).show()

                    // Navigate only after successful account creation
                    // (Android Developers, 2026d; Stack Overflow, 2013)
                    if (isAdded) {
                        (activity as MainActivity)
                            .replaceFragment(LoginFragment())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }
}