package com.example.betterbudget_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentAdapter(
    private val payments: List<DataClass.PaymentMethod>,
    private val onSetDefault: (String) -> Unit,
    private val onDelete: (String) -> Unit // (Kotlin, 2026)
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvPaymentType)
        val tvName: TextView = view.findViewById(R.id.tvPaymentName)
        val tvExpiry: TextView = view.findViewById(R.id.tvPaymentExpiry)
        val tvDefault: TextView = view.findViewById(R.id.tvDefaultBadge)
        val btnSetDefault: Button = view.findViewById(R.id.btnSetDefault)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    } // (Android Developers, 2026a)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }
    // (Android Developers, 2026b)

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]

        holder.tvType.text = payment.type
        holder.tvName.text = payment.name
        holder.tvExpiry.text = "Expiry: ${payment.expiryMonth}/${payment.expiryYear}"
        holder.tvDefault.visibility = if (payment.isDefault) View.VISIBLE else View.GONE

        holder.btnSetDefault.setOnClickListener { onSetDefault(payment.id) }
        holder.btnDelete.setOnClickListener { onDelete(payment.id) }
    }

    override fun getItemCount() = payments.size
}

/*Reference List:
Android Developers, 2026a. Create dynamic lists with RecyclerView. [online] Available at: https://developer.android.com/develop/ui/views/layout/recyclerview [Accessed 29 May 2026].
Android Developers, 2026b. LayoutInflater. [online] Available at: https://developer.android.com/reference/android/view/LayoutInflater [Accessed 29 May 2026].
Kotlin, 2026. Higher-order functions and lambdas. [online] Available at: https://kotlinlang.org/docs/lambdas.html [Accessed 29 May 2026].*/