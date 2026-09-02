package com.example.sms_parser.parser.bank

import com.example.sms_parser.data.ParsedTransaction
import com.example.sms_parser.data.TransactionType
import com.example.sms_parser.parser.BankParser
import com.example.sms_parser.util.SmsExtractors

class HSBCCardParser : BankParser {
    override fun canHandle(sender: String, message: String): Boolean {
        return message.contains("HSBC Credit Card", ignoreCase = true) &&
            (message.contains("used", ignoreCase = true) ||
             message.contains("spent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true) ||
             message.contains("credited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = SmsExtractors.extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = SmsExtractors.extractMerchant(message)
        val cardNumber = SmsExtractors.extractCardNumber(message)
        val date = SmsExtractors.extractDate(message)

        val transactionType = when {
            message.contains("credited", ignoreCase = true) -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            bankCode = "HSBC",
            bankName = "HSBC Credit Card",
            category = SmsExtractors.classifyMerchant(merchant),
            transactionType = transactionType,
            transactionDate = date,
            referenceId = merchant.hashCode().toString(),
            isFromCard = true
        )
    }
}
