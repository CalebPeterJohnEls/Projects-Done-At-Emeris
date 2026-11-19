package com.example.betterbudget_poe

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class InsightsFragment : Fragment() {
    private val categoryColors  = mutableMapOf<String, String>()  // name of hex color
    private val categoryMinLimits = mutableMapOf<String, Float>() // value of min
    private val categoryMaxLimits = mutableMapOf<String, Float>() // value of max
    private lateinit var db: DatabaseReference
    private lateinit var userId: String

    private lateinit var barChart: BarChart
    private lateinit var btnFromDate: Button
    private lateinit var btnToDate: Button
    private lateinit var btnApply: Button
    private lateinit var tvTotal: TextView
    private lateinit var tvEmpty: TextView

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Default range: start of current month → today
    private var fromDate: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    private var toDate: Calendar = Calendar.getInstance()

    private var valueEventListener: ValueEventListener? = null

    // Category order matches your spinner — keeps bar order consistent
    private var categories = listOf<String>()  // will be filled from Firebase

    // Bar colors mapped to your app's teal/green palette
    private val barColors = listOf(
        Color.parseColor("#4DC9A9"),
        Color.parseColor("#2C8E7E"),
        Color.parseColor("#41A466"),
        Color.parseColor("#50CCAB"),
        Color.parseColor("#1D9E75"),
        Color.parseColor("#0F6E56"),
        Color.parseColor("#9FE1CB")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_insights, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: ""

        barChart = view.findViewById(R.id.barChart)
        btnFromDate = view.findViewById(R.id.btnFromDate)
        btnToDate = view.findViewById(R.id.btnToDate)
        btnApply = view.findViewById(R.id.btnApply)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        setupChart()
        updateButtonLabels()

        btnFromDate.setOnClickListener { pickDate(isFrom = true) }
        btnToDate.setOnClickListener { pickDate(isFrom = false) }
        btnApply.setOnClickListener { loadInsights() }

        loadCategoryMeta()
    }

    // Sets up visual style of the chart — called once
    private fun setupChart() {
        barChart.apply { //(PhilJay, 2024; Stackoverflow, 2021)
            description.isEnabled  = false
            legend.isEnabled       = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            setBackgroundColor(Color.TRANSPARENT)
            setNoDataText("Loading...")
            setNoDataTextColor(Color.WHITE)
            setExtraOffsets(8f, 8f, 8f,30f)

            xAxis.apply { //(PhilJay, 2024; Stackoverflow, 2021)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.WHITE
                textSize = 10f
                labelRotationAngle = -30f
                isGranularityEnabled = true
                setAvoidFirstLastClipping(true)
            }

            axisLeft.apply { //(PhilJay, 2024; Stackoverflow, 2021)
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                textColor = Color.WHITE
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }
    private fun loadCategoryMeta() {
        db.child("categories").child(userId)
                .get() //(Firebase, 2026a) // (Firebase, 2026a)
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val userCategories = mutableListOf<String>()

                for (snap in snapshot.children) {
                    val cat = snap.getValue(com.example.betterbudget_poe.DataClass.Category::class.java)
                        ?: continue

                    categoryColors[cat.name] = cat.color
                    categoryMinLimits[cat.name] = cat.minLimit.toFloatOrNull() ?: 0f
                    categoryMaxLimits[cat.name] = cat.maxLimit.toFloatOrNull() ?: 0f

                    userCategories.add(cat.name)
                }
                // Sets the dynamic category list
                categories = userCategories

                // Loads chart data after meta is ready
                loadInsights()
            }
            .addOnFailureListener {
                if (isAdded) {
                    // If no categories present, uses empty list or falls back
                    categories = emptyList()
                    loadInsights()
                }
            }
    }
    // Attaches a live Firebase listener for the selected date range
    private fun loadInsights() {
        valueEventListener?.let {
            db.child("expenses").child(userId).removeEventListener(it)
        } //(Firebase, 2026a)

        val fromStr = sdf.format(fromDate.time)
        val toStr = sdf.format(toDate.time)


        if (fromDate.after(toDate)) {
            Toast.makeText(requireContext(), "From date must be before To date", Toast.LENGTH_SHORT).show()
            return
        }

        val listener = object : ValueEventListener { // (Firebase, 2026b)
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val totals = mutableMapOf<String, Double>()
                categories.forEach { totals[it] = 0.0 }

                val hasData = categories.isNotEmpty() && totals.values.any { it > 0 }
                tvEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
                barChart.visibility = if (hasData) View.VISIBLE else View.GONE
                // (PhilJay, 2024; Stackoverflow, 2021)

                for (expSnap in snapshot.children) {
                    try {
                        val date = expSnap.child("date").getValue(String::class.java) ?: continue
                        val category = expSnap.child("category").getValue(String::class.java) ?: continue

                        // Read amount safely — handles both String and Double stored values
                        val amount = try {
                            expSnap.child("amount").getValue(Double::class.java)
                        } catch (e: Exception) {
                            expSnap.child("amount").getValue(String::class.java)?.toDoubleOrNull()
                        } ?: continue

                        if (isInRange(date, fromStr, toStr)) {
                            totals[category] = (totals[category] ?: 0.0) + amount
                        }
                    } catch (e: Exception) {
                        // Skip any malformed records without crashing
                        android.util.Log.w("Insights", "Skipping malformed record: ${expSnap.key}", e)
                        continue
                    }
                }
                renderChart(totals)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded)
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        valueEventListener = listener
        db.child("expenses").child(userId).addValueEventListener(listener) // (Firebase, 2026b)
    }

    // Checks whether an expense date string falls within the selected range
    private fun isInRange(dateStr: String, fromStr: String, toStr: String): Boolean {
        return try {
            // Expenses use "d/M/yyyy" format (from AddExpenseFragment)
            val expSdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val expDate = expSdf.parse(dateStr) ?: return false
            val normalized = sdf.format(expDate)   // convert to "yyyy-MM-dd" for comparison
            normalized >= fromStr && normalized <= toStr
        } catch (e: Exception) { false }
    }

    // Builds and renders the bar chart from the aggregated totals map
    private fun renderChart(totals: Map<String, Double>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()
        var grandTotal = 0.0

        // Clear any previous limit lines
        barChart.axisLeft.removeAllLimitLines()

        categories.forEachIndexed { index, category ->
            val amount = totals[category] ?: 0.0
            entries.add(BarEntry(index.toFloat(), amount.toFloat()))
            labels.add(category)
            grandTotal += amount
            //(PhilJay, 2024; Stackoverflow, 2021)

            // Use Firebase category color, fall back to palette default
            val hexColor = categoryColors[category] ?: barColors[index % barColors.size]
                .let { "#%06X".format(0xFFFFFF and it) }
            colors.add(Color.parseColor(hexColor))

            // Draw min limit line if set
            val min = categoryMinLimits[category] ?: 0f
            if (min > 0f) {
                val minLine = com.github.mikephil.charting.components.LimitLine(min, "").apply {
                    lineColor = Color.parseColor("#FFC107")
                    lineWidth = 1.5f
                    enableDashedLine(10f, 5f, 0f)
                }
                barChart.axisLeft.addLimitLine(minLine)
            }

            // Draw max limit line if set
            val max = categoryMaxLimits[category] ?: 0f ////(PhilJay, 2024; Stackoverflow, 2021)
            if (max > 0f) {
                val maxLine = com.github.mikephil.charting.components.LimitLine(max, "").apply {
                    lineColor = Color.parseColor("#F44336")
                    lineWidth = 1.5f
                    enableDashedLine(10f, 5f, 0f)
                }
                barChart.axisLeft.addLimitLine(maxLine)
            }
        }

        val hasData = totals.values.any { it > 0 }
        tvEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        barChart.visibility = if (hasData) View.VISIBLE else View.GONE
        tvTotal.text = "Total: R%.2f".format(grandTotal)

        if (!hasData) return

        val dataSet = BarDataSet(entries, "Spending").apply { //(PhilJay, 2024; Stackoverflow, 2021)
            this.colors    = colors
            valueTextColor = Color.WHITE
            valueTextSize  = 10f
            setDrawValues(true)
        }

        barChart.apply { //(PhilJay, 2024; Stackoverflow, 2021)
            data = BarData(dataSet).apply { barWidth = 0.6f }
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            xAxis.isGranularityEnabled = true
            setVisibleXRangeMaximum(categories.size.toFloat())
            animateY(600)
            invalidate()
        }
    }

    // Shows a DatePickerDialog and updates fromDate or toDate
    private fun pickDate(isFrom: Boolean) { // (AndroidDevelopers, 2026)
        val cal = if (isFrom) fromDate else toDate
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                if (isFrom) {
                    fromDate = Calendar.getInstance().apply { set(year, month, day) }
                } else {
                    toDate = Calendar.getInstance().apply { set(year, month, day) }
                }
                updateButtonLabels()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateButtonLabels() {
        btnFromDate.text = "From: ${displaySdf.format(fromDate.time)}"
        btnToDate.text = "To: ${displaySdf.format(toDate.time)}"
    }

    // Clean up the live listener when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let {
            db.child("expenses").child(userId).removeEventListener(it) // (Firebase, 2026a)
        }
    }
}

/*Reference List:
Firebase, 2026a. Read and write data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 24 May 2026].
Firebase, 2026b. Listen for value events - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write#listen_for_value_events [Accessed 24 May 2026]..
Android Developers, 2026. DatePicker. [online] Available at: https://developer.android.com/reference/android/widget/DatePicker [Accessed 24 May 2026].
Oracle, 2026. Class SimpleDateFormat - Java SE 8. [online] Available at: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html [Accessed 24 May 2026].
PhilJay, 2024. MPAndroidChart - A powerful Android chart view / graph view library. [online] Available at: https://github.com/PhilJay/MPAndroidChart [Accessed 24 May 2026].
Stack Overflow, 2021. MPAndroidChart BarChart with different colors for each bar. [online] Available at: https://stackoverflow.com/questions/26784002/mpandroidchart-different-colors-for-each-bar [Accessed 24 May 2026].
*/