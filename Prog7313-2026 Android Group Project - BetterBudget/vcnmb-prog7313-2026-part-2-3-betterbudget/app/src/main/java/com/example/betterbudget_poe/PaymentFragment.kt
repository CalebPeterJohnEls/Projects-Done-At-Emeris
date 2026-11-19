package com.example.betterbudget_poe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PaymentFragment : Fragment() {

    private lateinit var db: DatabaseReference
    private lateinit var userId: String

    private lateinit var rvPayments: RecyclerView
    private lateinit var tvNoPayments: TextView
    private lateinit var fabAddPayment: FloatingActionButton

    private val paymentMethods = mutableListOf<DataClass.PaymentMethod>()
    private lateinit var adapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_payment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPayments = view.findViewById(R.id.rvPayments)
        tvNoPayments = view.findViewById(R.id.tvNoPayments)
        fabAddPayment = view.findViewById(R.id.fabAddPayment)

        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference // (Firebase, 2026a)

        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
            .getString("username", "") ?: "" // (AndroidDevelopers, 2026)

        rvPayments.layoutManager = LinearLayoutManager(requireContext())
        adapter = PaymentAdapter(paymentMethods, ::setDefaultPayment, ::deletePayment)
        rvPayments.adapter = adapter

        fabAddPayment.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(AddPaymentFragment())
        }

        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        db.child("payments").child(userId).addValueEventListener(object : ValueEventListener { //// (Firebase, 2026a)
            override fun onDataChange(snapshot: DataSnapshot) {
                paymentMethods.clear()
                for (snap in snapshot.children) {
                    val payment = snap.getValue(DataClass.PaymentMethod::class.java)
                    payment?.let { paymentMethods.add(it) }
                }

                tvNoPayments.visibility = if (paymentMethods.isEmpty()) View.VISIBLE else View.GONE
                rvPayments.visibility = if (paymentMethods.isEmpty()) View.GONE else View.VISIBLE

                adapter.notifyDataSetChanged()

                // Recalculate score whenever payment methods change
                // (AchievementsFragment companion object; Firebase, 2026a)
                AchievementsFragment.recalculateAndSaveRank(db, userId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setDefaultPayment(paymentId: String) {
        // Clear all defaults
        paymentMethods.forEach { it.isDefault = false }

        // Set selected as default
        val index = paymentMethods.indexOfFirst { it.id == paymentId }
        if (index != -1) {
            paymentMethods[index].isDefault = true

            // Update Firebase
            db.child("payments").child(userId).child(paymentId).child("isDefault")
                .setValue(true) // (Firebase, 2026a)
        }
    }

    private fun deletePayment(paymentId: String) {
        db.child("payments").child(userId).child(paymentId) // (Firebase, 2026a)
            .removeValue()
            .addOnSuccessListener {
                // Payment removed from list automatically via ValueEventListener
                AchievementsFragment.recalculateAndSaveRank(db, userId)
            }
    }
}

/*Reference List:
Firebase. 2026a. Read and Write Data on Android. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 26 May 2026].
Firebase. 2026b. Work with Lists of Data on Android. [online] Available at: https://firebase.google.com/docs/database/android/lists-of-data [Accessed 26 May 2026].
Android Developers. 2026. Save key-value data with SharedPreferences. [online] Available at: https://developer.android.com/training/data-storage/shared-preferences [Accessed 20 April 2026].*/
