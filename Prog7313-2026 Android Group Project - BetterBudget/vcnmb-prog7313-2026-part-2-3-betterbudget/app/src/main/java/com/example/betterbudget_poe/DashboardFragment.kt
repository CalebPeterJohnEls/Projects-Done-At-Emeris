package com.example.betterbudget_poe

import android.graphics.PorterDuff
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private lateinit var expenseRef: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var currentUser: String


    private val categoryColors = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance( // (Firebase, 2026a; Firebase, 2026b)
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        val sharedPref = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
        currentUser = sharedPref.getString("username", "User") ?: "User"

        val vwAddFunds = view.findViewById<View>(R.id.vwBalances)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val imgUser = view.findViewById<ImageView>(R.id.imgUser)
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val tvNoData = view.findViewById<TextView>(R.id.tvNoData)
        val imageRank = view.findViewById<ImageView>(R.id.imageRank)
        val vwViewGoals  = view.findViewById<View>(R.id.cdGoalProgress)
        val llGoalProgressContainer = view.findViewById<LinearLayout>(R.id.llGoalProgressContainer)
        loadGoalProgressCards(llGoalProgressContainer)
        val vwPayments = view.findViewById<View>(R.id.vwPayments) // (Android Developers, 2026e)
        tvUsername.text = currentUser
        // (Android Developers, 2026a; GeeksForGeeks, 2021)

        // Single database instance reused for both references


        // (Firebase, 2026a; Gosavi, 2020)
        db.child("users").child(currentUser)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                val imageUri = snapshot.child("imageUri").getValue(String::class.java) ?: ""
                val rank = snapshot.child("rank").getValue(String::class.java) ?: "Beginner Saver"

                tvUsername.text = if (name.isNotEmpty()) "$name $surname" else currentUser
                when (rank) {
                    "Beginner Saver"  -> imageRank.setImageResource(R.drawable.bronze)
                    "Smart Spender"   -> imageRank.setImageResource(R.drawable.silver)
                    "Budget Master"   -> imageRank.setImageResource(R.drawable.gold)
                    "Financial Guru"  -> imageRank.setImageResource(R.drawable.plat)
                    else              -> imageRank.setImageResource(R.drawable.ic_expenses)
                }
                if (imageUri.isNotEmpty()) {
                    try {
                        imgUser.setImageURI(Uri.parse(imageUri))
                    } catch (e: SecurityException) {
                        imgUser.setImageDrawable(null)
                    }
                }
            }

        vwPayments.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            (requireActivity() as MainActivity).replaceFragment(PaymentFragment())
        }

        vwAddFunds.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            (requireActivity() as MainActivity).replaceFragment(AddFundsFragment())
        }

        vwViewGoals.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            (requireActivity() as MainActivity).replaceFragment(GoalsFragment())
        }

        loadBalance()


        // (Firebase, 2026a; Firebase, 2026b)
        expenseRef = db.child("expenses").child(currentUser)

        // (GeeksForGeeks, 2022; Maima, 2022)
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 48f
        pieChart.transparentCircleRadius = 51f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Expenses"
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = Color.WHITE

        pieChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER
        pieChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
        pieChart.legend.xEntrySpace = 8f
        pieChart.legend.yEntrySpace = 4f
        pieChart.legend.textSize = 11f
        pieChart.setExtraOffsets(5f, 10f, 25f, 5f) // left, top, right, bottom

        // Load category colors from Firebase first, then render the chart —
        // same approach used in InsightsFragment.loadCategoryMeta()
        // (Firebase, 2026a; Skelton, 2017)
        loadCategoryColorsThenChart(pieChart, tvNoData)
    }

    // Fetches each category's hex color from the "categories" node, then
    // triggers the expense listener once the color map is populated.
    // Pattern taken directly from InsightsFragment.loadCategoryMeta()
    // (Firebase, 2026a; Skelton, 2017)

    private fun loadCurrentMonthSpending(tvAvailableDisplay: TextView?) {

        db.child("expenses").child(currentUser)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val currentCal = Calendar.getInstance()
                    val currentMonth = currentCal.get(Calendar.MONTH) + 1 // 1-12
                    val currentYear = currentCal.get(Calendar.YEAR)

                    var monthlyTotal = 0.0

                    for (expenseSnap in snapshot.children) {

                        val expense = expenseSnap.getValue(DataClass.Expense::class.java)
                            ?: continue

                        val parts = expense.date.split("/")

                        if (parts.size == 3) {

                            val month = parts[1].toIntOrNull() ?: continue
                            val year = parts[2].toIntOrNull() ?: continue

                            if (month == currentMonth && year == currentYear) {
                                monthlyTotal += expense.amount.toDoubleOrNull() ?: 0.0
                            }
                        }
                    }

                    tvAvailableDisplay?.text = "R %.2f".format(monthlyTotal)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadBalance() {
        val tvCurrentDisplay   = view?.findViewById<TextView>(R.id.tvCurrentDisplay)
        val tvAvailableDisplay = view?.findViewById<TextView>(R.id.tvAvailableDisplay)

        db.child("balances").child(currentUser).child("amount")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!isAdded) return

                    val balance = when {
                        snapshot.getValue(Double::class.java) != null ->
                            snapshot.getValue(Double::class.java)!!
                        snapshot.getValue(Long::class.java) != null ->
                            snapshot.getValue(Long::class.java)!!.toDouble()
                        else -> 0.0
                    }

                    tvCurrentDisplay?.text = "R %.2f".format(balance)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        loadCurrentMonthSpending(tvAvailableDisplay)
    }

    private fun loadGoalProgressCards(container: LinearLayout) {
        val userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: ""

        if (userId.isEmpty()) return

        db.child("goals").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                container.removeAllViews()

                if (!snapshot.exists()) {
                    val emptyText = TextView(requireContext()).apply {
                        text = "No goals set yet."
                        textSize = 14f
                        setTextColor(Color.parseColor("#AAAAAA"))
                        setPadding(32, 32, 32, 32)
                    }
                    container.addView(emptyText)
                    addAddGoalCard(container)
                    return@addOnSuccessListener
                }

                val goalList = snapshot.children.toList()
                var pending = goalList.size  // ← track async fetches

                for (snap in goalList) {
                    val category   = snap.child("category").getValue(String::class.java) ?: continue
                    val targetDate = snap.child("targetDate").getValue(String::class.java) ?: ""
                    val createdAt  = snap.child("createdAt").getValue(Long::class.java)
                    val percent    = calculateTimeElapsedPercent(createdAt, targetDate)
                    val daysLeft   = daysRemainingFrom(targetDate)
                    val displayDate = formatDisplayDate(targetDate)

                    db.child("categories").child(userId).get()
                        .addOnSuccessListener { catSnapshot ->
                            if (!isAdded) return@addOnSuccessListener

                            val categoryColor = catSnapshot.children
                                .mapNotNull { it.getValue(DataClass.Category::class.java) }
                                .find { it.name == category }
                                ?.color ?: "#4DC9A9"

                            addGoalProgressCard(
                                container, category, percent,
                                daysLeft, displayDate, categoryColor
                            )

                            pending--
                            if (pending == 0) addAddGoalCard(container)  // ← add last
                        }
                        .addOnFailureListener {
                            pending--
                            if (pending == 0) addAddGoalCard(container)
                        }
                }
            }
            .addOnFailureListener {
                if (isAdded)
                    Toast.makeText(requireContext(), "Error loading goals", Toast.LENGTH_SHORT).show()
            }
    }
    private fun addAddGoalCard(container: LinearLayout) {
        if (!isAdded) return
        val context = requireContext()
        val josefinBold = ResourcesCompat.getFont(context, R.font.josefin_sans_bold)

        val cardView = androidx.cardview.widget.CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(160.dpToPx(), 160.dpToPx()).apply {
                marginEnd = 12.dpToPx()
            }
            radius = 12.dpToPx().toFloat()
            cardElevation = 4.dpToPx().toFloat()
            setCardBackgroundColor(Color.parseColor("#1A4DC9A9"))
            isClickable = true
            isFocusable = true
            foreground = context.obtainStyledAttributes(
                intArrayOf(android.R.attr.selectableItemBackground)
            ).getDrawable(0)
            setOnClickListener {
                (requireActivity() as MainActivity).replaceFragment(GoalsFragment())
            }
        }

        val inner = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val icon = TextView(context).apply {
            text = "+"
            textSize = 36f
            typeface = josefinBold
            setTextColor(Color.parseColor("#4DC9A9"))
            gravity = android.view.Gravity.CENTER
        }

        val label = TextView(context).apply {
            text = "Add New Goal"
            textSize = 13f
            typeface = josefinBold
            setTextColor(Color.parseColor("#4DC9A9"))
            gravity = android.view.Gravity.CENTER
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 0)
        }

        inner.addView(icon)
        inner.addView(label)
        cardView.addView(inner)
        container.addView(cardView)
    }

    // Extension to convert dp → px cleanly
    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    private fun addGoalProgressCard(
        container: LinearLayout,
        category: String,
        percent: Int,
        daysRemaining: Long,
        displayDate: String,
        colorHex: String
    ) {
        if (!isAdded) return

        val cardView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_goal_progress, container, false)

        val tvCategory = cardView.findViewById<TextView>(R.id.tvGoalCategory)
        val tvDays  = cardView.findViewById<TextView>(R.id.tvGoalDays)
        val tvDate  = cardView.findViewById<TextView>(R.id.tvGoalDate)
        val tvPercent  = cardView.findViewById<TextView>(R.id.tvWheelPercent)
        val progress   = cardView.findViewById<ProgressBar>(R.id.progressWheel)

        tvCategory.text   = category
        tvDays.text       = if (daysRemaining > 0) "$daysRemaining days left" else "Goal ended"
        tvDate.text       = displayDate
        tvPercent.text    = "$percent%"
        progress.progress = percent

        try {
            val color = Color.parseColor(colorHex)

            // Ring turns red when the goal deadline has passed (100% elapsed)
            if (percent >= 100) {
                progress.progressDrawable
                    .mutate()
                    .setColorFilter(Color.parseColor("#F44336"), PorterDuff.Mode.SRC_IN)
                tvPercent.setTextColor(Color.parseColor("#F44336"))
            } else {
                progress.progressDrawable
                    .mutate()
                    .setColorFilter(color, PorterDuff.Mode.SRC_IN)
                tvPercent.setTextColor(color)
            }
        } catch (e: Exception) {
            // fall back to default teal if color parse fails
        }

        container.addView(cardView)
    }

    // Calculates how much of the goal's time has elapsed as a percentage.
// Uses createdAt timestamp if available, otherwise assumes goal started today.
    private fun calculateTimeElapsedPercent(createdAt: Long?, targetDateString: String): Int {
        if (targetDateString.isEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = sdf.parse(targetDateString)?.time ?: return 0
            val startDate = createdAt ?: System.currentTimeMillis()
            val now = System.currentTimeMillis()

            val totalDuration = targetDate - startDate
            if (totalDuration <= 0) return 100

            val elapsed = now - startDate
            ((elapsed.toFloat() / totalDuration.toFloat()) * 100)
                .toInt()
                .coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    // Formats "yyyy-MM-dd" to "Due: 15 Aug 2026" for display on the card
    private fun formatDisplayDate(targetDateString: String): String {
        if (targetDateString.isEmpty()) return "No due date"
        return try {
            val inputSdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date      = inputSdf.parse(targetDateString) ?: return "No due date"
            "Due: ${outputSdf.format(date)}"
        } catch (e: Exception) { "No due date" }
    }
    // Reuse the same days remaining helper as GoalsFragment
    private fun daysRemainingFrom(targetDateString: String): Long {
        if (targetDateString.isEmpty()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = sdf.parse(targetDateString) ?: return 0L
            val diff = target.time - System.currentTimeMillis()
            val days = diff / (1000L * 60 * 60 * 24)
            if (days < 0) 0L else days
        } catch (e: Exception) { 0L }
    }
    private fun loadCategoryColorsThenChart(pieChart: PieChart, tvNoData: TextView) {
        db.child("categories").child(currentUser).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                // Iterate snapshot children and map category name → hex color string
                // Same pattern as InsightsFragment.loadCategoryMeta()
                // (Firebase, 2026b; Skelton, 2017)
                for (snap in snapshot.children) {
                    val cat = snap.getValue(DataClass.Category::class.java) ?: continue
                    categoryColors[cat.name] = cat.color
                }

                // Now that colors are ready, attach the expense listener
                attachExpenseListener(pieChart, tvNoData)
            }
            .addOnFailureListener {
                // Fall back to rendering without colors if the read fails
                if (isAdded) attachExpenseListener(pieChart, tvNoData)
            }
    }

    // Attaches a live listener on the expenses node and renders the pie chart.
    // (Firebase, 2026a; Gosavi, 2020)
    private fun attachExpenseListener(pieChart: PieChart, tvNoData: TextView) {
        expenseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || context == null) return

                val expenses = snapshot.children.mapNotNull {
                    it.getValue(DataClass.Expense::class.java)
                }

                if (expenses.isEmpty()) {
                    tvNoData.visibility = View.VISIBLE
                    pieChart.visibility = View.GONE
                    return
                }

                tvNoData.visibility = View.GONE
                pieChart.visibility = View.VISIBLE

                // (Kotlin, 2026e; kt.academy, 2021)
                val categoryCounts: Map<String, Int> = expenses
                    .groupingBy { it.category ?: "Uncategorised" }
                    .eachCount()

                // (GeeksForGeeks, 2022; Yeung, 2020)
                val entries    = ArrayList<PieEntry>()
                val sliceColors = ArrayList<Int>()

                for ((category, count) in categoryCounts) {
                    entries.add(PieEntry(count.toFloat(), category))

                    // Use the hex color stored against this category in Firebase.
                    // Falls back to a default teal if the category has no color saved.
                    // Same fallback pattern used in InsightsFragment.renderChart()
                    val hex = categoryColors[category] ?: "#66CDAA"
                    sliceColors.add(parseColorSafe(hex))
                }

                // (GeeksForGeeks, 2022; Maima, 2022)
                val dataSet = PieDataSet(entries, "")
                dataSet.sliceSpace     = 3f
                dataSet.selectionShift = 5f
                dataSet.setDrawIcons(false)

                // Apply the per-category colors instead of a generic ColorTemplate
                // (InsightsFragment.renderChart(); Maima, 2022)
                dataSet.colors = sliceColors

                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(pieChart))
                data.setValueTextSize(13f)
                data.setValueTypeface(Typeface.DEFAULT_BOLD)
                data.setValueTextColor(Color.WHITE)

                pieChart.data = data
                pieChart.animateY(1400, Easing.EaseInOutQuad)
                pieChart.highlightValues(null)
                pieChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Safely parses a hex color string, falls back to teal if invalid.
    // Same helper used in CategoriesFragment
    private fun parseColorSafe(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.parseColor("#66CDAA")
        }
    }
}

//(IIE, 2026)
/*Reference list
- Android Developers, 2026a. Save key-value data with SharedPreferences. [online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 April 2026].
- Firebase, 2026a. Read and Write Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/read-and-write> [Accessed 28 April 2026].
- Firebase, 2026b. Work with Lists of Data on Android. [online] Available at: <https://firebase.google.com/docs/database/android/lists-of-data> [Accessed 18 April 2026].
- GeeksForGeeks, 2021. Shared Preferences in Android with Examples. [online] Available at: <https://www.geeksforgeeks.org/android/shared-preferences-in-android-with-examples/> [Accessed 18 April 2026].
- GeeksForGeeks, 2022. Android – Create a Pie Chart with Kotlin. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-create-a-pie-chart-with-kotlin/> [Accessed 27 May 2026].
- Gosavi, S., 2020. Upload data in Realtime Database and retrieve it. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026].
- The IIE. 2026. PROGRAMMING 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished.
- Kotlin, 2026e. Grouping – Kotlin Standard Library. [online] Available at: <https://kotlinlang.org/docs/collection-grouping.html> [Accessed 27 May 2026].
- kt.academy, 2021. Collection processing in Kotlin: Grouping elements. [online] Available at: <https://kt.academy/article/fk-cp-grouping> [Accessed 27 May 2026].
- Maima, M., 2022. (Kotlin) Implementing a Barchart and Piechart using MPAndroidChart. [blog] 2 June. Available at: <https://malcolmmaima.medium.com/kotlin-implementing-a-barchart-and-piechart-using-mpandroidchart-8c7643c4ba75> [Accessed 27 May 2026].
- PhilJay, 2026. MPAndroidChart. [online] Available at: <https://github.com/PhilJay/MPAndroidChart> [Accessed 27 May 2026].
- Skelton, N., 2017. Storing and retrieving data from Firebase with Kotlin on Android. [blog] 14 July. Available at: <https://medium.com/a-practical-guide-to-firebase-on-android/storing-and-retrieving-data-from-firebase-with-kotlin-on-android-91c36680771> [Accessed 1 June 2026].
- Yeung, C., 2020. Using MPAndroidChart for Android Application — PieChart. [blog] 9 September. Available at: <https://medium.com/@clyeung0714/using-mpandroidchart-for-android-application-piechart-123d62d4ddc0> [Accessed 27 May 2026].
- Android Developers, 2026b. LayoutInflater. [online] Available at: https://developer.android.com/reference/android/view/LayoutInflater [Accessed 2 June 2026].
- Android Developers, 2026c. ProgressBar. [online] Available at: https://developer.android.com/reference/android/widget/ProgressBar [Accessed 2 June 2026].
- Android Developers, 2026d. PorterDuff.Mode. [online] Available at: https://developer.android.com/reference/android/graphics/PorterDuff.Mode [Accessed 2 June 2026].
- Android Developers, 2026e. HorizontalScrollView. [online] Available at: https://developer.android.com/reference/android/widget/HorizontalScrollView [Accessed 2 June 2026].
*/