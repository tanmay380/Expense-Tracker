package com.example.sms_parser.data

data class ParsedTransaction(
    val isValid: Boolean,
    val amount: Double = 0.0,
    val merchant: String = "",
    val cardNumber: String = "",
    val accountNumber: String = "",
    val bankCode: String = "",
    val bankName: String = "",
    val category: String = "",
    val transactionType: TransactionType = TransactionType.DEBIT,
    val transactionDate: String = "",
    val referenceId: String = "",
    val isFromCard: Boolean = false,
)
