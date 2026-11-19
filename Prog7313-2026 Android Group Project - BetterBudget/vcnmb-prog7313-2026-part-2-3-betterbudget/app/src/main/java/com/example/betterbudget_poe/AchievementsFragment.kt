package com.example.betterbudget_poe

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.betterbudget_poe.DataClass.Goal
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AchievementsFragment : Fragment(R.layout.fragment_achievements) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvUserInfo = view.findViewById<TextView>(R.id.tvUserInfo)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val tvUserRank = view.findViewById<TextView>(R.id.tvUserRank)
        val tvUserScore = view.findViewById<TextView>(R.id.tvUserScore)
        val imageRank = view.findViewById<ImageView>(R.id.imageRank)
        val entryContainer = view.findViewById<LinearLayout>(R.id.entryContainer)
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
                    if (name.isNotEmpty()) "Welcome, $name $surname ($currency)"
                    else "Welcome, $currentUser ($currency)"
                tvUserScore.text = "Score: $score"
                tvUserRank.text = "Rank: $rank"
                // (Kotlin, 2026e)
                when (rank) {
                    "Beginner Saver" -> imageRank.setImageResource(R.drawable.bronze)
                    "Smart Spender" -> imageRank.setImageResource(R.drawable.silver)
                    "Budget Master" -> imageRank.setImageResource(R.drawable.gold)
                    "Financial Guru" -> imageRank.setImageResource(R.drawable.plat)
                    else -> imageRank.setImageResource(R.drawable.ic_expenses)
                }
                if (imageUri.isNotEmpty()) {
                    try { imageView.setImageURI(Uri.parse(imageUri)) }
                    catch (e: SecurityException) { imageView.setImageDrawable(null) }
                }
                // Read the saved breakdown list from Firebase and render rows
                // (Firebase, 2026a; Kotlin, 2026f)
                @Suppress("UNCHECKED_CAST")
                val breakdown = (snapshot.child("breakdown").value as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()
                renderBreakdown(entryContainer, breakdown, score, rank)
            }
        // (Android Developers, 2026d; Stack Overflow, 2013)
        btnBack.setOnClickListener {
            activity?.let { (it as MainActivity).replaceFragment(ProfileFragment()) }
        }
    }

    // Builds one row per breakdown line, matching the expense list style
    // (Android Developers, 2026h; Android Developers, 2026i)
    private fun renderBreakdown(
        container: LinearLayout,
        lines: List<String>,
        totalScore: Int,
        rank: String
    ) {
        if (!isAdded) return
        container.removeAllViews()
        if (lines.isEmpty()) {
            // (Android Developers, 2026i)
            val empty = TextView(requireContext()).apply {
                text = "No points yet. Add a goal or log an expense to start earning!"
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(20, 40, 20, 40)
            }
            container.addView(empty)
            return
        }
        // Reusable row builder — horizontal layout matching ExpensesFragment rows
        // (Android Developers, 2026h; Android Developers, 2026i)
        fun addRow(label: String, subtitle: String, pts: String) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 24, 20, 24)
            }
            // Left side: label + subtitle stacked vertically
            // (Android Developers, 2026h)
            val textBlock = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            // (Android Developers, 2026i)
            val tvLabel = TextView(requireContext()).apply {
                text = label
                textSize = 16f
                setTextColor(Color.WHITE)
            }
            // (Android Developers, 2026i)
            val tvSub = TextView(requireContext()).apply {
                text = subtitle
                textSize = 13f
                setTextColor(Color.parseColor("#AAAAAA"))
                setPadding(0, 4, 0, 0)
            }
            textBlock.addView(tvLabel)
            textBlock.addView(tvSub)
            // Right side: points value
            // (Android Developers, 2026i)
            val tvPts = TextView(requireContext()).apply {
                text = pts
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            row.addView(textBlock)
            row.addView(tvPts)
            // Thin divider between rows
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                setBackgroundColor(Color.parseColor("#33FFFFFF"))
            }
            container.addView(row)
            container.addView(divider)
        }
        // Render each breakdown line as a row — strings match exactly what
        // recalculateAndSaveRank saves to Firebase
        // (Kotlin, 2026e; Firebase, 2026a)
        lines.forEach { line ->
            when {
                line.startsWith("+100: Goal completed") -> {
                    val category = line.removePrefix("+100: Goal completed (").removeSuffix(")")
                    addRow("Goal completed", category, "+100 pts")
                }
                line.startsWith("+20: Goal added") -> {
                    val category = line.removePrefix("+20: Goal added (").removeSuffix(")")
                    addRow("Goal added", category, "+20 pts")
                }
                line.startsWith("+20: Expense added") -> {
                    // (Android Developers, 2026i)
                    addRow("Expense logged", "", "+20 pts")
                }
                line.startsWith("+20: Payment Method added") -> {
                    // (Android Developers, 2026i)
                    addRow("Payment method added", "", "+20 pts")
                }
            }
        }
        // Total row at the bottom — no divider after it
        // (Android Developers, 2026h; Android Developers, 2026i)
        val totalRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 24, 20, 24)
        }
        // (Android Developers, 2026i)
        val tvTotalLabel = TextView(requireContext()).apply {
            text = "Total score"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        // (Android Developers, 2026i)
        val tvTotalPts = TextView(requireContext()).apply {
            text = "$totalScore pts"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
        totalRow.addView(tvTotalLabel)
        totalRow.addView(tvTotalPts)
        container.addView(totalRow)
    }

    companion object {

        fun calculateScore(goals: List<Goal>): Int {
            var score = 0
            goals.forEach { goal ->
                score += 20
                val progress = goal.goalProgress.toDoubleOrNull() ?: 0.0
                val target = goal.maxLimit.toDoubleOrNull() ?: 0.0
                if (target > 0 && progress >= target) score += 100
            }
            return score
        }

        fun calculatePayMethScore(paymentMethodCount: Int): Int {
            return paymentMethodCount * 20
        }

        fun calculateExpenseScore(expenseCount: Int): Int {
            return expenseCount * 20
        }

        fun getRank(score: Int): String {
            // (Kotlin, 2026e)
            return when {
                score >= 500 -> "Financial Guru"
                score >= 300 -> "Budget Master"
                score >= 150 -> "Smart Spender"
                else -> "Beginner Saver"
            }
        }

        // Rebuilds score + breakdown from goals, expenses and payment methods,
        // then saves everything to Firebase in one final update
        // (Firebase, 2026a; Firebase, 2026c)
        fun recalculateAndSaveRank(db: DatabaseReference, userId: String) {
            val goalsRef = db.child("goals").child(userId)
            val expenseRef = db.child("expenses").child(userId)
            val paymentRef = db.child("payments").child(userId)
            // (Firebase, 2026c)
            goalsRef.get().addOnSuccessListener { goalsSnap ->
                val goals = mutableListOf<Goal>()
                val breakdown = mutableListOf<String>()
                // (Firebase, 2026d)
                for (snap in goalsSnap.children) {
                    snap.getValue(Goal::class.java)?.let { goal ->
                        goals.add(goal)
                        breakdown.add("+20: Goal added (${goal.category})")
                        val progress = goal.goalProgress.toDoubleOrNull() ?: 0.0
                        val target = goal.maxLimit.toDoubleOrNull() ?: 0.0
                        if (target > 0 && progress >= target) {
                            breakdown.add("+100: Goal completed (${goal.category})")
                        }
                    }
                }
                val goalScore = calculateScore(goals)
                // (Firebase, 2026c)
                expenseRef.get().addOnSuccessListener { expSnap ->
                    val expenseCount = expSnap.childrenCount.toInt()
                    val expenseScore = calculateExpenseScore(expenseCount)
                    // One line per expense so renderBreakdown can match each one
                    // (Kotlin, 2026e)
                    repeat(expenseCount) {
                        breakdown.add("+20: Expense added")
                    }
                    // (Firebase, 2026c)
                    paymentRef.get().addOnSuccessListener { paySnap ->
                        val paymentCount = paySnap.childrenCount.toInt()
                        val paymentScore = calculatePayMethScore(paymentCount)
                        // One line per payment method so renderBreakdown can match each one
                        // (Kotlin, 2026e)
                        repeat(paymentCount) {
                            breakdown.add("+20: Payment Method added")
                        }
                        // Single combined total from all three sources
                        val total = goalScore + expenseScore + paymentScore
                        val rank = getRank(total)
                        // (Firebase, 2026b)
                        db.child("users").child(userId).updateChildren(
                            mapOf("score" to total, "rank" to rank, "breakdown" to breakdown)
                        )
                    }
                }
            }
        }


        fun saveRank(db: DatabaseReference, userId: String, score: Int) {
            val rank = getRank(score)
            // (Firebase, 2026b)
            db.child("users").child(userId).updateChildren(
                mapOf("score" to score, "rank" to rank)
            )
        }
    }
}

/*
* Reference List:
* Android Developers, 2026d. Fragment Transactions. [online] Available at: <https://developer.android.com/guide/fragments/transactions> [Accessed 19 April 2026].
* Android Developers, 2026f. Save key-value data with SharedPreferences. [online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 April 2026].
* Android Developers, 2026h. LinearLayout. [online] Available at: <https://developer.android.com/reference/android/widget/LinearLayout> [Accessed 20 May 2026].
* Android Developers, 2026i. TextView. [online] Available at: <https://developer.android.com/reference/android/widget/TextView> [Accessed 20 May 2026].
* Firebase, 2026a. Read and Write Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/read-and-write> [Accessed 28 April 2026].
* Firebase, 2026b. Work with Lists of Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/lists-of-data> [Accessed 18 April 2026].
* Firebase, 2026c. Update specific fields - Firebase Realtime Database. [online] Available at: <https://firebase.google.com/docs/database/android/read-and-write#update_specific_fields> [Accessed 20 May 2026].
* Firebase, 2026d. Work with lists of data on Android - Firebase Realtime Database. [online] Available at: <https://firebase.google.com/docs/database/android/lists-of-data> [Accessed 20 May 2026].
* GeeksForGeeks, 2021. Shared Preferences in Android with Examples. [online] Available at: <https://www.geeksforgeeks.org/android/shared-preferences-in-android-with-examples/> [Accessed 18 April 2026].
* Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026].
* Kotlin, 2026e. Control flow: when expressions and statements. [online] Available at: <https://kotlinlang.org/docs/control-flow.html#when-expressions-and-statements> [Accessed 20 May 2026].
* Kotlin, 2026f. filterIsInstance – Kotlin Standard Library. [online] Available at: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/filter-is-instance.html> [Accessed 20 May 2026].
* Stack Overflow, 2013. Fragment not attached to activity. [online] Available at: <https://stackoverflow.com/questions/10919240/fragment-myfragment-not-attached-to-activity> [Accessed 28 April 2026].
*/