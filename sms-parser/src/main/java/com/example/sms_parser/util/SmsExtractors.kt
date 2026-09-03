package com.example.sms_parser.util

import java.util.UUID

object SmsExtractors {
    fun generateTransactionId(): String = UUID.randomUUID().toString()

    fun extractAmount(message: String): Double? {
        val patterns = listOf(
            Regex("""(?:INR|Rs\.?)\s*([\d,]+(?:\.\d{2})?)"""),
            Regex("""(\d+(?:\.\d{2})?)\s*(?:debited|spent|sent|credited)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }

    fun extractMerchant(message: String): String {
        val patterns = listOf(
            Regex("""(?:at|on|to)\s+([A-Z][A-Z0-9\s&-]{2,30})(?:\s+FOOD|\s+on|\s+for|\s+via|[.,])"""),
            Regex("""(?:at|on|to)\s+([A-Z][A-Z0-9\s&-]{2,30})$""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim().replace(Regex("""\s+"""), " ")
            }
        }
        return "Unknown Merchant"
    }

    fun extractCardNumber(message: String): String {
        val patterns = listOf(
            Regex("""(?:card|Card)\s+(?:ending\s+with\s+)?(?:xx|XX)(\d{4})"""),
            Regex("""(?:xx|XX)(\d{4})"""),
            Regex("""ending\s+with\s+(\d{4})""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return ""
    }

    fun extractAccountNumber(message: String): String {
        val patterns = listOf(
            Regex("""(?:Acc|Account|a/c)\s+(?:xx|XX)(\d{4})"""),
            Regex("""(?:xx|XX)(\d{3,4})""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return ""
    }

    fun extractReferenceId(message: String): String {
        val patterns = listOf(
            Regex("""(?:Ref|Ref No)[.:\s]*(\d{10,})"""),
            Regex("""UPI Ref[.:\s]*(\d{10,})"""),
            Regex("""Retrieval Ref No[.:\s]*(\d{10,})""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return UUID.randomUUID().toString().takeLast(10)
    }

    fun extractDate(message: String): String {
        val patterns = listOf(
            Regex("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})"""),
            Regex("""on\s+(\d{1,2}-[A-Za-z]+-\d{2})""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return ""
    }

    fun classifyMerchant(merchant: String): String {
        val text = merchant.lowercase()
        return when {
            text.contains(Regex("(fuel|petrol|diesel|gas station|pump)")) -> "Fuel"
            text.contains(Regex("(upi|paytm|phonepe|google pay|gpay|bhim)")) -> "UPI"
            text.contains(Regex("(credit card|credit|bill payment|cc bill)")) -> "Credit Card Bill"
            text.contains(Regex("(salary|neft|credit|transfer|received|income)")) -> "Salary"
            text.contains(Regex("(cashback|refund|reward)")) -> "Cashback"
            text.contains(Regex("(amazon|flipkart|ebay|myntra|shopping|mall|store|groceries|blinkit|zepto)")) -> "Personal"
            else -> "Personal"
        }
    }

    fun extractMerchantFromAccount(message: String): String {
        val patterns = listOf(
            Regex("""towards\s+([A-Za-z0-9\s&-]{2,50})(?:\s+for|\s+Ref|$)"""),
            Regex("""for\s+([A-Za-z0-9\s&-]{2,50})$""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return "Bank Transfer"
    }

    fun extractMerchantFromSavings(message: String): String {
        val patterns = listOf(
            Regex("""towards\s+([A-Za-z0-9\s&*-]{2,50})(?:\s+for|$)"""),
            Regex("""for\s+([A-Za-z0-9\s&*-]{2,50})$""")
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return "Account Debit"
    }
}
