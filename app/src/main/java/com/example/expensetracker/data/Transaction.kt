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
    val categoryId: String? = null,
    val accountId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "sms", // "sms" or "manual"
    val smsSender: String? = null,
    val smsContent: String? = null,
    val isIncome: Boolean = false,
)