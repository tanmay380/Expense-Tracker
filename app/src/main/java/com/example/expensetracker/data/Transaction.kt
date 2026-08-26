package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = "",
    val merchant: String = "",
    val amount: Double = 0.0,
    val category: String = "Shopping",
    val accountId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "sms", // "sms" or "manual"
    val smsSender: String? = null,
    val smsContent: String? = null,
    val isIncome: Boolean = false,
)

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
