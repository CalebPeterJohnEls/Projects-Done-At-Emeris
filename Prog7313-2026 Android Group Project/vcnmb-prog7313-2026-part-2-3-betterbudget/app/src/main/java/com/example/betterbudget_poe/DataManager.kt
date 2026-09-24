package com.example.betterbudget_poe

object DataManager { //Kotlin A, 2026
    val categories = mutableListOf<CategoryData.Category>()
    val expenses = mutableListOf<CategoryData.Expense>()

    private var categoryIdCounter = 1

    fun addCategory(name: String) { //Kotlin A, 2026
        val newId = (categories.maxOfOrNull { it.id } ?: 0) + 1
        categories.add(CategoryData.Category(newId, name))
    }

    fun updateCategory(category: CategoryData.Category, name: String) { //Kotlin A, 2026
        category.name = name
        //category.color = color
    }

    fun deleteCategory(category: CategoryData.Category) { //Kotlin A, 2026
        categories.remove(category)

        // Remove related expenses
        expenses.removeAll { it.category.id == category.id }
    }

    fun addExpense(title: String, amount: Double, category: CategoryData.Category) {
        expenses.add(CategoryData.Expense(title, amount, category))
    }
}