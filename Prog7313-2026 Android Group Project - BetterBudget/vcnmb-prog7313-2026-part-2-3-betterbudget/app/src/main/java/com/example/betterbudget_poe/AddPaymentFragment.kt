package com.example.betterbudget_poe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AddPaymentFragment : Fragment() {

    private lateinit var db: DatabaseReference
    private lateinit var userId: String

    private lateinit var spinnerPaymentType: Spinner
    private lateinit var edtPaymentName: EditText
    private lateinit var edtPaymentNumber: EditText
    private lateinit var edtExpiryMonth: EditText
    private lateinit var edtExpiryYear: EditText
    private lateinit var chkDefault: CheckBox
    private lateinit var btnSavePayment: Button
    private lateinit var btnCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_payment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerPaymentType = view.findViewById(R.id.spinnerPaymentType) //  (Android Developers, 2026b)
        edtPaymentName = view.findViewById(R.id.edtPaymentName)
        edtPaymentNumber = view.findViewById(R.id.edtPaymentNumber)
        edtExpiryMonth = view.findViewById(R.id.edtExpiryMonth)
        edtExpiryYear = view.findViewById(R.id.edtExpiryYear)
        chkDefault = view.findViewById(R.id.chkDefault) // (Android Developers, 2026c)
        btnSavePayment = view.findViewById(R.id.btnSavePayment)
        btnCancel = view.findViewById(R.id.btnCancel)

        db = FirebaseDatabase.getInstance(
            "https://betterbudget-fbf48-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference // (Firebase, 2026b)

        userId = requireContext()
            .getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE) // (AndroidDevelopers, 2026a)
            .getString("username", "") ?: ""

        btnSavePayment.setOnClickListener { savePayment() }
        btnCancel.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(PaymentFragment()) // (Android Developers, 2026d)
        }
    }

    private fun savePayment() {
        val type = spinnerPaymentType.selectedItem.toString()
        val name = edtPaymentName.text.toString().trim()
        val number = edtPaymentNumber.text.toString().trim()
        val expiryMonth = edtExpiryMonth.text.toString().trim()
        val expiryYear = edtExpiryYear.text.toString().trim()

        if (name.isEmpty() || number.isEmpty() || expiryMonth.isEmpty() || expiryYear.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (expiryMonth.length != 2 || expiryYear.length != 4) {
            Toast.makeText(requireContext(), "Invalid expiry date format", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentId = UUID.randomUUID().toString() // (Oracle, 2026)
        val isDefault = chkDefault.isChecked

        // If setting as default, unset other default methods
        if (isDefault) {
            clearDefaultPayments()
        }

        val paymentMethod = DataClass.PaymentMethod(
            id = paymentId,
            type = type,
            name = name,
            number = number,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            isDefault = isDefault
        )

        db.child("payments").child(userId).child(paymentId) // (Firebase, 2026a)
            .setValue(paymentMethod)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Payment method saved", Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).replaceFragment(PaymentFragment())
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearDefaultPayments() {
        db.child("payments").child(userId).get() // (AndroidDevelopers, 2026)
            .addOnSuccessListener { snapshot ->
                for (snap in snapshot.children) {
                    val key = snap.key ?: continue
                    db.child("payments").child(userId).child(key)
                        .child("isDefault")
                        .setValue(false) // (Android Developers, 2026a)
                }
            }
    }
}

/*Reference List:
Android Developers, 2026a. Save key-value data with SharedPreferences. [online] Available at: https://developer.android.com/training/data-storage/shared-preferences [Accessed 26 May 2026].
Android Developers, 2026b. Spinner. [online] Available at: https://developer.android.com/reference/android/widget/Spinner [Accessed 2 June 2026].
Android Developers, 2026c. CheckBox. [online] Available at: https://developer.android.com/reference/android/widget/CheckBox [Accessed 2 June 2026].
Android Developers, 2026d. Fragment transactions. [online] Available at: https://developer.android.com/guide/fragments/transactions [Accessed 2 June 2026].
Firebase, 2026a. Read and write data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/read-and-write [Accessed 26 May 2026].
Firebase, 2026b. Work with lists of data on Android - Firebase Realtime Database. [online] Available at: https://firebase.google.com/docs/database/android/lists-of-data [Accessed 26 May 2026].
Oracle, 2026. Class UUID - Java SE 8. [online] Available at: https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html [Accessed 2 June 2026].*/
