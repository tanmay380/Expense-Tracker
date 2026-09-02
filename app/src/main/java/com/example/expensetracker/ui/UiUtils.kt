package com.example.expensetracker.ui

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object UiUtils {
    fun formatAmount(amount: Double): String {
        val sign = if (amount >= 0) "+" else ""
        return String.format("%s₹%,.2f", sign, kotlin.math.abs(amount))
    }

    fun formatAmountCompact(amount: Double): String {
        val sign = if (amount >= 0) "+" else ""
        return String.format("%s₹%,.0f", sign, kotlin.math.abs(amount))
    }

    fun formatDate(timestamp: Long): String {
        val datetime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        val today = LocalDate.now()
        val txnDate = datetime.toLocalDate()

        return when {
            txnDate == today -> "Today"
            txnDate == today.minusDays(1) -> "Yesterday"
            else -> DateTimeFormatter.ofPattern("EEE, dd MMM").format(datetime)
        }
    }

    fun formatTime(timestamp: Long): String {
        val datetime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return DateTimeFormatter.ofPattern("h:mm a").format(datetime)
    }

    fun getColorForCategory(category: String): Color {
        return when (category) {
            "Groceries" -> Color(0xFF7A8A5E)
            "Eating out" -> Color(0xFF8C491A)
            "Travel" -> Color(0xFF474238)
            "Bills" -> Color(0xFFB2622D)
            "Shopping" -> Color(0xFF728157)
            "Health" -> Color(0xFF3D472B)
            "Rent" -> Color(0xFF2E2B25)
            "Salary" -> Color(0xFF3D472B)
            else -> Color(0xFF645C50)
        }
    }

    fun getBackgroundColorForCategory(category: String): Color {
        return when (category) {
            "Groceries" -> Color(0xFF7A8A5E).copy(alpha = 0.24f)
            "Eating out" -> Color(0xFFC67139).copy(alpha = 0.2f)
            "Travel" -> Color(0xFF645C50).copy(alpha = 0.2f)
            "Bills" -> Color(0xFFC67139).copy(alpha = 0.14f)
            "Shopping" -> Color(0xFF7A8A5E).copy(alpha = 0.18f)
            "Health" -> Color(0xFF7A8A5E).copy(alpha = 0.3f)
            "Rent" -> Color(0xFF645C50).copy(alpha = 0.26f)
            "Salary" -> Color(0xFF7A8A5E).copy(alpha = 0.28f)
            else -> Color(0xFF645C50).copy(alpha = 0.16f)
        }
    }
}

object AppColors {
    val Primary = Color(0xFFC67139)
    val Surface = Color(0xFFF5EAD8)
    val Background = Color(0xFFEFE3CD)
    val CardBackground = Color(0xFFFDF8F0)
    val TextPrimary = Color(0xFF201E1D)
    val TextSecondary = Color(0xFF645C50)
    val Text = Color(0xFF201E1D)
    val Divider = Color(0xFF201E1D).copy(alpha = 0.1f)

    val IncomeGreen = Color(0xFF7A8A5E)
    val ExpenseRed = Color(0xFF8C491A)
}
