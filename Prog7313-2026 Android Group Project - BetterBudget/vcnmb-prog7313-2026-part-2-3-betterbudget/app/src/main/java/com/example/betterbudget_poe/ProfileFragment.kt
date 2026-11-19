package com.example.betterbudget_poe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import android.widget.PopupMenu
import android.widget.Toast

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUserInfo = view.findViewById<TextView>(R.id.tvUserInfo)
        val btnEditUser = view.findViewById<Button>(R.id.btnEditUser)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnAchieve = view.findViewById<Button>(R.id.btnAchieve)
        val tvUserRank = view.findViewById<TextView>(R.id.tvUserRank)
        val tvUserScore = view.findViewById<TextView>(R.id.tvUserScore)
        val btnTheme = view.findViewById<Button>(R.id.btnTheme)

        // (Android Developers, 2026f; GeeksForGeeks, 2021)
        val sharedPref = requireActivity()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("username", "") ?: ""

        // (Firebase, 2026a; Gosavi, 2020)
        FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users").child(currentUser)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                val imageUri = snapshot.child("imageUri").getValue(String::class.java) ?: ""
                val currency = snapshot.child("currency").getValue(String::class.java) ?: ""

                val score = snapshot.child("score").getValue(Int::class.java) ?: 0
                val rank = snapshot.child("rank").getValue(String::class.java) ?: "Beginner Saver"

                tvUserInfo.text =
                    if (name.isNotEmpty())
                        "Welcome, $name $surname ($currency)"
                    else
                        "Welcome, $currentUser ($currency)"

                tvUserScore.text = "Score: $score"
                tvUserRank.text = "Rank: $rank"

                if (imageUri.isNotEmpty()) {
                    try {
                        imageView.setImageURI(Uri.parse(imageUri))
                    } catch (e: SecurityException) {
                        imageView.setImageDrawable(null)
                    }
                }
            }

        btnEditUser.setOnClickListener {
            (activity as MainActivity).replaceFragment(EditProfileFragment())
        }
        // (Android Developers, 2026d; Stack Overflow, 2013)
        btnLogout.setOnClickListener {
            activity?.let {
                (it as MainActivity).replaceFragment(WelcomeFragment())
            }
        }
        // (Android Developers, 2026d; Stack Overflow, 2013)
        btnAchieve.setOnClickListener {
            activity?.let {
                (it as MainActivity).replaceFragment(AchievementsFragment())
            }
        }
        btnTheme.setOnClickListener {
            val popup = PopupMenu(requireContext(), btnTheme)
            popup.menuInflater.inflate(R.menu.menu_themes, popup.menu)

            // Highlight the currently active theme
            val currentTheme = requireContext()
                .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
                .getString("bubbleTheme", "default")

            val checkId = when (currentTheme) {
                "light"   -> R.id.theme_light
                "ocean"   -> R.id.theme_ocean
                "sunset"  -> R.id.theme_sunset
                else      -> R.id.theme_default
            }
            popup.menu.findItem(checkId)?.isChecked = true

            popup.setOnMenuItemClickListener { item ->
                val themeKey = when (item.itemId) {
                    R.id.theme_default -> "default"
                    R.id.theme_light   -> "light"
                    R.id.theme_ocean   -> "ocean"
                    R.id.theme_sunset  -> "sunset"
                    else               -> "default"
                }
                (requireActivity() as MainActivity).applyAndSaveBubbleTheme(themeKey)
                Toast.makeText(requireContext(), "Theme changed to ${item.title}", Toast.LENGTH_SHORT).show()
                true
            }
            popup.show()
        }

    }
    }
