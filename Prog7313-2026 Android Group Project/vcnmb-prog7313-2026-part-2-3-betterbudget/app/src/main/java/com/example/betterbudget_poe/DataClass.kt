package com.example.betterbudget_poe
class DataClass {
    data class Expense(
        val date: String = "",
        val startTime: String = "",
        val endTime: String = "",
        val amount: String = "",
        val description: String = "",
        val category: String = "",
        val imageUri: String = ""
    )

    data class User(
        val username: String = "",
        val email: String = "",
        val password: String = ""
    )

    data class Goal(
        var id: String = "",
        val category: String = "",
        val minLimit: String = "",
        val maxLimit: String = "",
        val goalProgress: String = "",
        val userId: String = "",
        val targetDate: String = "",
        val createdAt: Long = System.currentTimeMillis()  // add this
    ) {
        constructor() : this("", "", "", "", "", "", "", 0L)
    }
    data class Category(
        val userId: String = "",
        val name: String = "",
        val color: String = "",
        val minLimit: String = "",
        val maxLimit: String = ""
    )
    {
        constructor() : this("","","","","")
    }

    data class PaymentMethod(
        val id: String = "",
        val type: String = "",
        val name: String = "",
        val number: String = "",
        val expiryMonth: String = "",
        val expiryYear: String = "",
        var isDefault: Boolean = false,
        val timeStamp: Long = System.currentTimeMillis()
    )
}