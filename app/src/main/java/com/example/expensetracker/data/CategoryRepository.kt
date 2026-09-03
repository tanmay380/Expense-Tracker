package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: String): Category? =
        categoryDao.getCategoryById(id)

    suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getCategoryByName(name)

    suspend fun addCategory(category: Category) =
        categoryDao.insert(category)

    suspend fun updateCategory(category: Category) =
        categoryDao.update(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category)

    suspend fun initializeDefaultCategories() {
        val existingSalary = categoryDao.getCategoryByName("Salary")
        if (existingSalary == null) {
            val defaultCategories = listOf(
                // Income categories
                Category("income_salary", "Salary", CategoryType.INCOME, false),
                Category("income_cashback", "Cashback", CategoryType.INCOME, false),
                Category("income_rewards", "Rewards", CategoryType.INCOME, false),
                // Expense categories
                Category("expense_fuel", "Fuel", CategoryType.EXPENSE, false),
                Category("expense_upi", "UPI", CategoryType.EXPENSE, false),
                Category("expense_creditcard", "Credit Card Bill", CategoryType.EXPENSE, false),
                Category("expense_personal", "Personal", CategoryType.EXPENSE, false),
            )
            defaultCategories.forEach { categoryDao.insert(it) }
        }
    }
}
