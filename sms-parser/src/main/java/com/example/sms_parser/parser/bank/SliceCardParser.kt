package com.example.sms_parser.parser.bank

import com.example.sms_parser.data.ParsedTransaction
import com.example.sms_parser.data.TransactionType
import com.example.sms_parser.parser.BankParser
import com.example.sms_parser.util.SmsExtractors

class SliceCardParser : BankParser {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("SLICE", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("sent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true) ||
             message.contains("credited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = SmsExtractors.extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = SmsExtractors.extractMerchant(message)
        val cardNumber = SmsExtractors.extractCardNumber(message)
        val accountNumber = SmsExtractors.extractAccountNumber(message)
        val referenceId = SmsExtractors.extractReferenceId(message)
        val date = SmsExtractors.extractDate(message)

        val transactionType = when {
            message.contains("credited", ignoreCase = true) -> TransactionType.CREDIT
            message.contains("sent", ignoreCase = true) -> TransactionType.TRANSFER
            else -> TransactionType.DEBIT
        }

        val isFromCard = cardNumber.isNotEmpty()

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            accountNumber = accountNumber,
            bankCode = "SLICE",
            bankName = "Slice Bank",
            category = SmsExtractors.classifyMerchant(merchant),
            transactionType = transactionType,
            transactionDate = date,
            referenceId = referenceId,
            isFromCard = isFromCard
        )
    }
}
