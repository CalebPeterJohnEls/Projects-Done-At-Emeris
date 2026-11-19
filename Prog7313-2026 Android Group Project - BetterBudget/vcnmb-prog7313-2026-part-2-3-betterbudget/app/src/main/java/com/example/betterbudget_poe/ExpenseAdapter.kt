package com.example.betterbudget_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter( //GeeksforGeeks B, 2025
    private val expenses: MutableList<CategoryData.Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val amount: TextView = view.findViewById(R.id.txtAmount)
        val category: TextView = view.findViewById(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //w3school, 2026
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = expenses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //w3school, 2026
        val expense = expenses[position]

        holder.title.text = expense.title
        holder.amount.text = "R ${expense.amount}"
        holder.category.text = "Category: ${expense.category.name}"
    }
}