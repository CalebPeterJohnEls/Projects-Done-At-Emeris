package com.example.betterbudget_poe

import android.content.Context
import com.example.betterbudget_poe.DataClass.Goal
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class GoalsFragment : Fragment() {
    private lateinit var db: DatabaseReference
    private lateinit var userId: String
    private lateinit var llGoalsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference
        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: ""
        llGoalsContainer = view.findViewById(R.id.llGoalsContainer)
        val fabAddGoal = view.findViewById<FloatingActionButton>(R.id.fabAddGoal)
        fabAddGoal.setOnClickListener {
            (activity as MainActivity).replaceFragment(AddGoalFragment())
        }
        loadGoals()
    }

    private fun loadGoals() {
        db.child("goals").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                llGoalsContainer.removeAllViews()
                if (!snapshot.exists()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }
                val goalsList = mutableListOf<Goal>()
                for (goalSnap in snapshot.children) {
                    val goal = goalSnap.getValue(Goal::class.java)
                    goal?.let {
                        it.id = goalSnap.key ?: ""
                        goalsList.add(it)
                        addGoalView(it)
                    }
                }
                // Recalculates score from both goals AND expenses before saving
                AchievementsFragment.recalculateAndSaveRank(db, userId)
            }
            .addOnFailureListener {
                if (isAdded) Toast.makeText(
                    requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
    }

    private fun addGoalView(goal: Goal) {
        if (!isAdded) return
        val goalView = LayoutInflater.from(context)
            .inflate(R.layout.item_goal, llGoalsContainer, false)
        val tvCategory     = goalView.findViewById<TextView>(R.id.tvCategory)
        val tvAmounts      = goalView.findViewById<TextView>(R.id.tvAmounts)
        val tvPercent      = goalView.findViewById<TextView>(R.id.tvPercent)
        val progressBar    = goalView.findViewById<ProgressBar>(R.id.progressBar)
        val btnEdit        = goalView.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete      = goalView.findViewById<ImageButton>(R.id.btnDelete)
        val tvDaysRemaining = goalView.findViewById<TextView>(R.id.tvDaysRemaining)

        val progress       = goal.goalProgress.toDoubleOrNull() ?: 0.0
        val maxLimit       = goal.maxLimit.toDoubleOrNull() ?: 1.0
        val percent        = if (maxLimit > 0) ((progress / maxLimit) * 100).toInt() else 0
        val clampedPercent = percent.coerceIn(0, 100)
        val days           = daysRemainingFrom(goal.targetDate)

        tvDaysRemaining.text =
            if (goal.targetDate.isEmpty()) "No end date set"
            else "$days day(s) remaining"
        tvCategory.text  = goal.category
        tvAmounts.text   = "R${progress.toInt()} / R${maxLimit.toInt()}"
        tvPercent.text   = "$clampedPercent%"
        progressBar.progress = clampedPercent

        if (percent > 100) {
            progressBar.progressDrawable
                .setColorFilter(Color.parseColor("#F44336"), PorterDuff.Mode.SRC_IN)
            tvPercent.setTextColor(Color.parseColor("#F44336"))
        } else {
            progressBar.progressDrawable
                .setColorFilter(Color.parseColor("#4DC9A9"), PorterDuff.Mode.SRC_IN)
            tvPercent.setTextColor(Color.parseColor("#4DC9A9"))
        }

        btnEdit.setOnClickListener   { editGoal(goal) }
        btnDelete.setOnClickListener { deleteGoal(goal) }
        llGoalsContainer.addView(goalView)
    }

    private fun showEmptyState() {
        if (!isAdded) return
        val emptyView = TextView(context).apply {
            text      = "No goals yet. Tap + to add one."
            textSize  = 16f
            gravity   = Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        llGoalsContainer.addView(emptyView)
    }

    fun daysRemainingFrom(targetDateString: String): Long {
        if (targetDateString.isEmpty()) return 0L
        return try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = sdf.parse(targetDateString) ?: return 0L
            val diff   = target.time - System.currentTimeMillis()
            val days   = diff / (1000L * 60 * 60 * 24)
            if (days < 0) 0L else days
        } catch (e: Exception) { 0L }
    }

    private fun editGoal(goal: Goal) {
        val fragment = AddGoalFragment()
        val bundle   = Bundle()
        bundle.putString("goalId", goal.id)
        fragment.arguments = bundle
        (activity as MainActivity).replaceFragment(fragment)
    }

    private fun deleteGoal(goal: Goal) {
        db.child("goals").child(userId).child(goal.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show()
                // Recalculate after deletion so score drops correctly
                AchievementsFragment.recalculateAndSaveRank(db, userId)
            }
    }
}