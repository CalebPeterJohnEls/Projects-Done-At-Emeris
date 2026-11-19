package com.example.betterbudget_poe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.betterbudget_poe.DataClass.PaymentMethod
import com.google.firebase.database.*


class AddFundsFragment : Fragment() {

    private lateinit var db: DatabaseReference
    private lateinit var userId: String

    private lateinit var spinnerPayments: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnAddFunds: Button
    private lateinit var btnCancel: Button
    private lateinit var tvNoPayments: TextView

    private val paymentMethods = mutableListOf<PaymentMethod>()
    private val paymentLabels  = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_funds, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: "" // (Android Developers, 2026a)

        spinnerPayments = view.findViewById(R.id.spinnerPayments)
        etAmount  = view.findViewById(R.id.etFundsAmount)
        btnAddFunds = view.findViewById(R.id.btnAddFunds)
        btnCancel = view.findViewById(R.id.btnCancelFunds)
        tvNoPayments = view.findViewById(R.id.tvNoPayments)

        btnCancel.setOnClickListener {
            (activity as MainActivity).replaceFragment(DashboardFragment())
        }

        loadPaymentMethods()

        btnAddFunds.setOnClickListener { addFunds() }
    }

    // Loads the user's saved payment methods into the spinner
    private fun loadPaymentMethods() {
        db.child("payments").child(userId).get() // (Android Developers, 2026a)
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                paymentMethods.clear()
                paymentLabels.clear()

                for (snap in snapshot.children) {
                    val method = snap.getValue(PaymentMethod::class.java) ?: continue // (Firebase, 2026)
                    paymentMethods.add(method)

                    // Show last 4 digits of card number for identification
                    val last4  = if (method.number.length >= 4)
                        method.number.takeLast(4) else method.number
                    val label  = "${method.type} — ${method.name} (****$last4)"
                    paymentLabels.add(label)
                }

                if (paymentMethods.isEmpty()) {
                    tvNoPayments.visibility  = View.VISIBLE
                    spinnerPayments.visibility = View.GONE
                    btnAddFunds.isEnabled    = false
                    return@addOnSuccessListener
                }

                tvNoPayments.visibility  = View.GONE
                spinnerPayments.visibility = View.VISIBLE
                btnAddFunds.isEnabled    = true

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    paymentLabels
                )
                spinnerPayments.adapter = adapter

                // Pre-select the default payment method if one is set
                val defaultIndex = paymentMethods.indexOfFirst { it.isDefault }
                if (defaultIndex >= 0) spinnerPayments.setSelection(defaultIndex) // (Android Developers, 2026b)
            }
            .addOnFailureListener {
                if (isAdded)
                    Toast.makeText(requireContext(), "Failed to load payment methods", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addFunds() {
        val amountText = etAmount.text.toString().trim()
        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            return
        }

        if (paymentMethods.isEmpty()) {
            Toast.makeText(requireContext(), "No payment method selected", Toast.LENGTH_SHORT).show()
            return
        }

        btnAddFunds.isEnabled = false

        // Read current balance, add the new amount, then write it back
        db.child("balances").child(userId).child("amount").get() // (Android Developers, 2026a)
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val currentBalance = when { // (Firebase, 2026)
                    snapshot.getValue(Double::class.java) != null ->
                        snapshot.getValue(Double::class.java)!!
                    snapshot.getValue(Long::class.java) != null ->
                        snapshot.getValue(Long::class.java)!!.toDouble()
                    else -> 0.0
                }

                val newBalance = currentBalance + amount

                db.child("balances").child(userId).child("amount")
                    .setValue(newBalance) // (Firebase, 2026)
                    .addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener
                        Toast.makeText(
                            requireContext(),
                            "R%.2f added successfully".format(amount),
                            Toast.LENGTH_SHORT
                        ).show()
                        (activity as MainActivity).replaceFragment(DashboardFragment())
                    }
                    .addOnFailureListener { e ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            btnAddFunds.isEnabled = true
                        }
                    }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to read balance: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnAddFunds.isEnabled = true
                }
            }
    }
}
/*Reference List:
Android Developers, 2026a. Save key-value data with SharedPreferences. [online] Available at: https://developer.android.com/training/data-storage/shared-preferences [Accessed 29 May 2026].
Android Developers, 2026b. ArrayAdapter. [online] Available at: https://developer.android.com/reference/android/widget/ArrayAdapter [Accessed 29 May 2026].
Firebase, 2026. Read and Write Data on Android. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 29 May 2026].
*/