package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.funinactionproto.SplashFragment
import com.example.funinactionproto.PracticeFragment
import com.example.funinactionproto.ProfileFragment
import com.example.funinactionproto.ProgressFragment
import com.example.funinactionproto.R
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.navBar)

        enableImmersiveMode()

        //App always starts on the splash screen, which then decides where to go next
        replaceFragment(SplashFragment())

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> { replaceFragment(HomeFragment()); true }
                R.id.ic_practice -> { replaceFragment(PracticeFragment()); true }
                R.id.nav_learn -> { replaceFragment(LearnFragment()); true }
                R.id.ic_progress -> { replaceFragment(ProgressFragment()); true }
                R.id.ic_profile -> { replaceFragment(ProfileFragment()); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            replaceFragment(SplashFragment())
        }
    }

    //Same pattern as BetterBudget, swaps fragments and shows or hides the bottom nav
    fun replaceFragment(
        fragment: Fragment,
        addToBackStack: Boolean = false
    ) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        bottomNav.visibility =
            if (
                fragment is SplashFragment ||
                fragment is WelcomeFragment ||
                fragment is TutorialFragment ||
                fragment is LoginFragment ||
                fragment is RegisterFragment
            ) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun enableImmersiveMode() {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(
            window,
            window.decorView
        )

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}