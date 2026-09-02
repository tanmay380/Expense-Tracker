package com.example.sms_parser.parser.bank

import com.example.sms_parser.data.ParsedTransaction
import com.example.sms_parser.data.TransactionType
import com.example.sms_parser.parser.BankParser
import com.example.sms_parser.util.SmsExtractors

class AxisParser : BankParser {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("AXIS", ignoreCase = true) &&
            (message.contains("Credit Card", ignoreCase = true) ||
             message.contains("Bank Acc", ignoreCase = true) ||
             message.contains("Savings Account", ignoreCase = true)) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true) ||
             message.contains("credited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = SmsExtractors.extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val referenceId = SmsExtractors.extractReferenceId(message)
        val date = SmsExtractors.extractDate(message)

        val transactionType = when {
            message.contains("credited", ignoreCase = true) -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }

        val isFromCard = message.contains("Credit Card", ignoreCase = true)

        return if (isFromCard) {
            val merchant = SmsExtractors.extractMerchant(message)
            val cardNumber = SmsExtractors.extractCardNumber(message)

            ParsedTransaction(
                isValid = true,
                amount = amount,
                merchant = merchant,
                cardNumber = cardNumber,
                bankCode = "AXIS",
                bankName = "Axis Bank Credit Card",
                category = SmsExtractors.classifyMerchant(merchant),
                transactionType = transactionType,
                transactionDate = date,
                referenceId = referenceId,
                isFromCard = true
            )
        } else {
            val merchant = SmsExtractors.extractMerchantFromAccount(message)
            val accountNumber = SmsExtractors.extractAccountNumber(message)

            ParsedTransaction(
                isValid = true,
                amount = amount,
                merchant = merchant,
                accountNumber = accountNumber,
                bankCode = "AXIS",
                bankName = "Axis Bank Account",
                category = SmsExtractors.classifyMerchant(merchant),
                transactionType = transactionType,
                transactionDate = date,
                referenceId = referenceId,
                isFromCard = false
            )
        }
    }
}
