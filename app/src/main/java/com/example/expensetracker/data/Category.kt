package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE, // INCOME or EXPENSE
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class CategoryType {
    INCOME, EXPENSE
}
