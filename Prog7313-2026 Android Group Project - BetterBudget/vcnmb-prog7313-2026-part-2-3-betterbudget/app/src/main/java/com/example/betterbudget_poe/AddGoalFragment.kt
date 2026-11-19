package com.example.betterbudget_poe

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.betterbudget_poe.DataClass.Category
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.betterbudget_poe.DataClass.Goal

class AddGoalFragment : Fragment() {

    private lateinit var db: DatabaseReference
    private lateinit var userId: String
    private var editingGoalId: String? = null
    private lateinit var goalDate: DatePicker

    private lateinit var spinnerCategory: Spinner
    private lateinit var etMinLimit: EditText
    private lateinit var etMaxLimit: EditText
    private lateinit var btnSaveGoal: Button
    private lateinit var btnBack: Button

    private var selectedCategory: String = ""
    private var selectedCategoryColor: String = "#66CDAA"


    private val loadedCategories = mutableListOf<Category>()
    private val categoryDisplayNames = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_goal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        // (Firebase, 2026a)
        spinnerCategory = view.findViewById(R.id.spnCategories)
        etMinLimit  = view.findViewById(R.id.edtMinLimit)
        etMaxLimit = view.findViewById(R.id.edtMaxLimit)
        goalDate = view.findViewById(R.id.tdDate)
        btnSaveGoal = view.findViewById(R.id.btnAdd)
        btnBack = view.findViewById(R.id.btnBack)

        val today = Calendar.getInstance()
        goalDate.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH),
            null
        )
        //(Android Developers, 2026a; Android Developers, 2026b)

        editingGoalId = arguments?.getString("goalId")

        editingGoalId?.let { btnSaveGoal.text = "Update Goal" }

        // Load categories from Firebase first, then populate spinner
        loadCategoriesFromFirebase()

        btnSaveGoal.setOnClickListener { // (Firebase, 2026c)
            saveGoal()
            Toast.makeText(context, "Entry saved!", Toast.LENGTH_SHORT).show()

            // Navigate to expenses fragment after saving
            // (Android Developers, 2026d; Stack Overflow, 2013)
            (activity as MainActivity).replaceFragment(GoalsFragment())}
            btnBack.setOnClickListener {
            (activity as MainActivity).replaceFragment(GoalsFragment())
        }
    }

    // Pulls categories saved under the user's node in Firebase
    private fun loadCategoriesFromFirebase() {
        db.child("categories").child(userId)
            .get() //(Firebase, 2026a)
            .addOnSuccessListener { snapshot -> // (Firebase, 2026c)
                if (!isAdded) return@addOnSuccessListener

                loadedCategories.clear()
                categoryDisplayNames.clear()
                categoryDisplayNames.add("Select a category")

                for (snap in snapshot.children) {
                    val category = snap.getValue(Category::class.java) ?: continue // (Firebase, 2026d)
                    loadedCategories.add(category)
                    categoryDisplayNames.add(category.name)
                }

                if (loadedCategories.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No categories found. Please add a category first.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                setupSpinner()

                // If editing, load the goal after categories are ready
                editingGoalId?.let { loadGoalForEdit(it) }
            }
            .addOnFailureListener {
                if (isAdded)
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
    }

    // Binds the loaded category names to the spinner
    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryDisplayNames
        )
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    selectedCategory      = ""
                    selectedCategoryColor = "#66CDAA"
                } else {
                    // position - 1 because index 0 is "Select a category"
                    val category = loadedCategories[position - 1]
                    selectedCategory = category.name
                    selectedCategoryColor = category.color
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory      = ""
                selectedCategoryColor = "#66CDAA"
            }
        }
    }

    private fun getTargetDateString(): String {
        val cal = Calendar.getInstance()
        cal.set(goalDate.year, goalDate.month, goalDate.dayOfMonth)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun loadGoalForEdit(goalId: String) {
        db.child("goals").child(userId).child(goalId)
            .get() //(Firebase, 2026a)
            .addOnSuccessListener { snapshot -> // (Firebase, 2026c)
                if (!isAdded) return@addOnSuccessListener
                val goal = snapshot.getValue(Goal::class.java) ?: return@addOnSuccessListener //// (Firebase, 2026d)

                etMinLimit.setText(goal.minLimit)
                etMaxLimit.setText(goal.maxLimit)

                // Match saved category name back to spinner position
                val position = categoryDisplayNames.indexOf(goal.category)
                if (position >= 0) {
                    spinnerCategory.setSelection(position)
                    selectedCategory = goal.category
                    selectedCategoryColor = loadedCategories
                        .find { it.name == goal.category }?.color ?: "#66CDAA"
                }

                if (goal.targetDate.isNotEmpty()) {
                    try {
                        val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) //(Oracle, 2026)
                        val date = sdf.parse(goal.targetDate)
                        val cal  = Calendar.getInstance()
                        if (date != null) {
                            cal.time = date
                            goalDate.updateDate(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                            //(Android Developers, 2026a; Android Developers, 2026b)
                        }
                    } catch (e: Exception) {  }
                }
            }
            .addOnFailureListener { // (Firebase, 2026c)
                if (isAdded)
                    Toast.makeText(requireContext(), "Failed to load goal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGoal() {
        if (selectedCategory.isEmpty()) {
            Toast.makeText(requireContext(), "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val minLimitText  = etMinLimit.text.toString().trim()
        val minLimitValue = minLimitText.toDoubleOrNull()
        if (minLimitValue == null || minLimitValue <= 0) {
            etMinLimit.error = "Enter valid minimum amount"; return
        }

        val maxLimitText  = etMaxLimit.text.toString().trim()
        val maxLimitValue = maxLimitText.toDoubleOrNull()
        if (maxLimitValue == null || maxLimitValue <= 0) {
            etMaxLimit.error = "Enter valid maximum amount"; return
        }

        val targetDate = getTargetDateString()

        btnSaveGoal.isEnabled = false

        if (editingGoalId == null) {
            val goalId = db.child("goals").child(userId).push().key
            if (goalId == null) {
                Toast.makeText(requireContext(), "Error creating goal ID", Toast.LENGTH_SHORT).show()
                btnSaveGoal.isEnabled = true
                return
            }

            val goal = Goal(
                id = goalId,
                category= selectedCategory,
                minLimit= minLimitText,
                maxLimit= maxLimitText,
                goalProgress = "",
                userId = userId,
                targetDate = targetDate
            )

            db.child("goals").child(userId).child(goalId)
                .setValue(goal) //(Firebase, 2026a)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    // Write min/max limits back to the category node so
                    // InsightsFragment can read them for the chart lines
                    updateCategoryLimits(selectedCategory, minLimitText, maxLimitText)

                    Toast.makeText(requireContext(), "Goal saved", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e -> // (Firebase, 2026c)
                    if (isAdded)
                        Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSaveGoal.isEnabled = true
                }

        } else {
            val updates = mapOf(
                "category" to selectedCategory,
                "minLimit" to minLimitText,
                "maxLimit" to maxLimitText,
                "targetDate" to targetDate
            )
            db.child("goals").child(userId).child(editingGoalId!!)
                .updateChildren(updates) //(Firebase, 2026b)
                .addOnSuccessListener { // (Firebase, 2026c)
                    if (!isAdded) return@addOnSuccessListener
                    updateCategoryLimits(selectedCategory, minLimitText, maxLimitText)
                    Toast.makeText(requireContext(), "Goal updated", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e -> // (Firebase, 2026c)
                    if (isAdded)
                        Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSaveGoal.isEnabled = true
                }
        }
    }
    // Writes the goal's min/max back onto the matching category
    private fun updateCategoryLimits(categoryName: String, min: String, max: String) {
        db.child("categories").child(userId)
            .get() //(Firebase, 2026a)
            .addOnSuccessListener { snapshot ->
                for (snap in snapshot.children) {
                    val cat = snap.getValue(Category::class.java) ?: continue // (Firebase, 2026d)
                    if (cat.name == categoryName) {
                        snap.ref.updateChildren(
                            mapOf("minLimit" to min, "maxLimit" to max)
                        )
                        break
                    }
                }
            }
    }
}

/* Reference List:
Android Developers, 2026a. DatePicker. [online] Available at: https://developer.android.com/reference/android/widget/DatePicker [Accessed 24 May 2026].
Android Developers, 2026b. Calendar. [online] Available at: https://developer.android.com/reference/java/util/Calendar [Accessed 24 May 2026].
Firebase, 2026a. Read and write data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 24 May 2026].
Firebase, 2026b. Update specific fields - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write#update_specific_fields [Accessed 24 May 2026].
Firebase, 2026c. Listen for value events - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write#listen_for_value_events [Accessed 24 May 2026]..
Firebase, 2026d. Work with lists of data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/lists-of-data [Accessed 24 May 2026].
Oracle, 2026. Class SimpleDateFormat - Java SE 8. [online] Available at: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html [Accessed 24 May 2026].
 */