package com.example.betterbudget_poe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.betterbudget_poe.DashboardFragment
import com.example.betterbudget_poe.ProfileFragment
import com.example.betterbudget_poe.SignUpFragment
import com.example.betterbudget_poe.LoginFragment
import com.example.betterbudget_poe.WelcomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var bubbleBackground: BubbleBackgroundView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        hideSystemUI()

        bubbleBackground = findViewById(R.id.bubbleBackground)
        bottomNav = findViewById(R.id.navBar)
        //we check if the SharedPreference session exists
        val sharedPref = getSharedPreferences("ExpencesPrefs", Context.MODE_PRIVATE)
        val prefs = getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        val savedTheme = prefs.getString("bubbleTheme", "default") ?: "default"
        bubbleBackground.post { bubbleBackground.applyTheme(savedTheme) }
        //checks if the user has been logged in
        if (sharedPref.contains("username")) {
            replaceFragment(DashboardFragment()) //if logged in they go to this screen
        } else {
            replaceFragment(WelcomeFragment()) //appears if nobody is logged in
        }

        //Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    replaceFragment(DashboardFragment())
                    true
                }

                R.id.ic_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                R.id.ic_expenses -> {
                    replaceFragment(ExpensesFragment())
                    true
                }

                R.id.ic_categories -> {
                    replaceFragment(CategoriesFragment())
                    true
                }

                R.id.ic_insights -> {
                    replaceFragment(InsightsFragment())
                    true
                }

                else -> false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        hideSystemUI() // re-hides if user swipes it back temporarily
    }
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    //ReplaceFragment switches fragments and controls id navbar is visible or not
    fun replaceFragment(fragment: Fragment) {
        val switchFrag = supportFragmentManager.beginTransaction()
        switchFrag.replace(R.id.fragment_container, fragment)
            .commitNow()
        if (fragment is SignUpFragment || fragment is LoginFragment || fragment is WelcomeFragment) {
            bottomNav.visibility = View.GONE
        } else {
            bottomNav.visibility = View.VISIBLE
        }

    }

    fun applyAndSaveBubbleTheme(themeKey: String) {
        bubbleBackground.applyTheme(themeKey)
        getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("bubbleTheme", themeKey)
            .apply()
    }
}



