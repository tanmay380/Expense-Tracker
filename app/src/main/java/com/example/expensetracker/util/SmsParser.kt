package com.example.expensetracker.util

import java.util.UUID

data class ParsedSms(
    val amount: Double,
    val merchant: String,
    val accountNumber: String,
    val bankCode: String,
    val isIncome: Boolean,
)

object SmsParser {
    private val amountPattern = Regex("""(?:INR|Rs\.?)\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
    private val accountPattern = Regex("""(?:A/c\s*XX|Account\s*XX|Card\s*XX)([0-9]{4})\b""", RegexOption.IGNORE_CASE)
    private val debitPattern = Regex("""(?:debited|spent|paid)\b""", RegexOption.IGNORE_CASE)
    private val creditPattern = Regex("""(?:credited|received|transferred in)\b""", RegexOption.IGNORE_CASE)

    fun parseTransaction(sms: String, sender: String): ParsedSms? {
        val amount = extractAmount(sms) ?: return null
        val merchant = extractMerchant(sms) ?: "Unknown"
        val accountNumber = extractAccountNumber(sms) ?: "0000"
        val isIncome = creditPattern.containsMatchIn(sms)

        return ParsedSms(
            amount = amount,
            merchant = merchant,
            accountNumber = accountNumber,
            bankCode = extractBankCode(sender),
            isIncome = isIncome
        )
    }

    private fun extractAmount(sms: String): Double? {
        val match = amountPattern.find(sms)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractAccountNumber(sms: String): String? {
        val match = accountPattern.find(sms)
        return match?.groupValues?.get(1)
    }

    private fun extractMerchant(sms: String): String? {
        // Look for patterns like "to MERCHANT" or "at MERCHANT"
        val merchantPattern = Regex("""(?:to|at|towards)\s+([A-Z][A-Z\s]+?)(?:\s+on|\s+via|\.|\s*$)""", RegexOption.IGNORE_CASE)
        val match = merchantPattern.find(sms)
        return match?.groupValues?.get(1)?.trim()?.replace(Regex("\\s+"), " ")
    }

    private fun extractBankCode(sender: String): String {
        // Extract bank code from sender (e.g., "VM-ICICIB" -> "ICICI")
        return when {
            sender.contains("ICICI", ignoreCase = true) -> "ICICI"
            sender.contains("HDFC", ignoreCase = true) -> "HDFC"
            sender.contains("SBI", ignoreCase = true) -> "SBI"
            sender.contains("AXIS", ignoreCase = true) -> "AXIS"
            sender.contains("SLICE", ignoreCase = true) -> "SLICE"
            sender.contains("PAYTM", ignoreCase = true) -> "PAYTM"
            else -> sender.uppercase()
        }
    }

    fun isBankMessage(sender: String): Boolean {
        val bankPatterns = listOf("ICICI", "HDFC", "SBI", "AXIS", "SLICE", "PAYTM", "KOTAK", "IDBI", "YES", "UNION", "9315926219")
        return bankPatterns.any { sender.contains(it, ignoreCase = true) }
    }

    fun generateTransactionId(): String = UUID.randomUUID().toString()
}
