package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val bankCode: String = "",
    val accountNumber: String = "",
    val initialBalance: Double = 0.0,
    val color: String = "#C67139", // hex color
    val isActive: Boolean = true,
)
