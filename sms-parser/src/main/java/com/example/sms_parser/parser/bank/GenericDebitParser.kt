package com.example.sms_parser.parser.bank

import com.example.sms_parser.data.ParsedTransaction
import com.example.sms_parser.data.TransactionType
import com.example.sms_parser.parser.BankParser
import com.example.sms_parser.util.SmsExtractors

class GenericDebitParser : BankParser {
    override fun canHandle(sender: String, message: String): Boolean {
        return (message.contains("debited", ignoreCase = true) ||
                message.contains("spent", ignoreCase = true) ||
                message.contains("credited", ignoreCase = true)) &&
                !message.contains("Credit Card", ignoreCase = true)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = SmsExtractors.extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = SmsExtractors.extractMerchant(message)
        val accountNumber = SmsExtractors.extractAccountNumber(message)
        val bankCode = extractBankCodeFromSender(sender)
        val date = SmsExtractors.extractDate(message)

        val transactionType = when {
            message.contains("credited", ignoreCase = true) -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            accountNumber = accountNumber,
            bankCode = bankCode,
            bankName = extractBankNameFromSender(sender),
            category = SmsExtractors.classifyMerchant(merchant),
            transactionType = transactionType,
            transactionDate = date,
            referenceId = merchant.hashCode().toString(),
            isFromCard = false
        )
    }

    private fun extractBankCodeFromSender(sender: String): String {
        return when {
            sender.contains("ICICI", ignoreCase = true) -> "ICICI"
            sender.contains("SBI", ignoreCase = true) -> "SBI"
            sender.contains("HDFC", ignoreCase = true) -> "HDFC"
            sender.contains("AXIS", ignoreCase = true) -> "AXIS"
            sender.contains("SLICE", ignoreCase = true) -> "SLICE"
            else -> sender.uppercase().take(4)
        }
    }

    private fun extractBankNameFromSender(sender: String): String {
        return when {
            sender.contains("ICICI", ignoreCase = true) -> "ICICI Bank"
            sender.contains("SBI", ignoreCase = true) -> "SBI"
            sender.contains("HDFC", ignoreCase = true) -> "HDFC Bank"
            sender.contains("AXIS", ignoreCase = true) -> "Axis Bank"
            sender.contains("SLICE", ignoreCase = true) -> "Slice Bank"
            else -> sender.uppercase()
        }
    }
}
