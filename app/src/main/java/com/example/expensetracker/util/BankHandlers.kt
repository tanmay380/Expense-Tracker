package com.example.expensetracker.util

// SLICE BANK Handler
class SliceBankHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("slice", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("sent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val cardNumber = extractCardNumber(message)
        val accountNumber = extractAccountNumber(message)
        val referenceId = extractReferenceId(message)
        val date = extractDate(message)

        val transactionType = when {
            message.contains("spent", ignoreCase = true) -> TransactionType.DEBIT
            message.contains("sent", ignoreCase = true) -> TransactionType.TRANSFER
            message.contains("debited", ignoreCase = true) -> TransactionType.DEBIT
            else -> TransactionType.DEBIT
        }

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            accountNumber = accountNumber,
            bankCode = "SLICE",
            bankName = "Slice Bank",
            category = classifyMerchant(merchant),
            transactionType = transactionType,
            transactionDate = date,
            referenceId = referenceId
        )
    }
}

// SBI CARD Handler
class SBICardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("SBI", ignoreCase = true) &&
            message.contains("SBI Credit Card", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val cardNumber = extractCardNumber(message)
        val referenceId = extractReferenceId(message)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            bankCode = "SBI",
            bankName = "SBI Credit Card",
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = referenceId
        )
    }
}

// HSBC CARD Handler
class HSBCCardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return message.contains("HSBC Credit Card", ignoreCase = true) &&
            (message.contains("used", ignoreCase = true) ||
             message.contains("spent", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val cardNumber = extractCardNumber(message)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            bankCode = "HSBC",
            bankName = "HSBC Credit Card",
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = merchant.hashCode().toString()
        )
    }
}

// ICICI CARD Handler
class ICICICardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("ICICI Bank Card", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val cardNumber = extractCardNumber(message)
        val referenceId = extractReferenceId(message)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            bankCode = "ICICI",
            bankName = "ICICI Bank Card",
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = referenceId
        )
    }
}

// ICICI ACCOUNT Handler
class ICICIAccountHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("ICICI Bank Acc", ignoreCase = true) &&
            message.contains("debited", ignoreCase = true)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchantFromAccount(message)
        val accountNumber = extractAccountNumber(message)
        val referenceId = extractReferenceId(message)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            accountNumber = accountNumber,
            bankCode = "ICICI",
            bankName = "ICICI Bank Account",
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = referenceId
        )
    }

    private fun extractMerchantFromAccount(message: String): String {
        // For account transfers, extract description instead of merchant
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
}

// ICICI SAVINGS ACCOUNT Handler
class ICICISavingsAccountHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("Savings Account", ignoreCase = true) &&
            message.contains("debited", ignoreCase = true)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchantFromSavings(message)
        val accountNumber = extractAccountNumber(message)
        val referenceId = extractReferenceId(message)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            accountNumber = accountNumber,
            bankCode = "ICICI",
            bankName = "ICICI Savings Account",
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = referenceId
        )
    }

    private fun extractMerchantFromSavings(message: String): String {
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

// Generic Credit Card Handler (fallback for other banks)
class GenericCreditCardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return message.contains("Credit Card", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) ||
             message.contains("used", ignoreCase = true) ||
             message.contains("debited", ignoreCase = true))
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val cardNumber = extractCardNumber(message)
        val bankName = extractBankName(sender, message)
        val bankCode = extractBankCode(sender)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            cardNumber = cardNumber,
            bankCode = bankCode,
            bankName = bankName,
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = merchant.hashCode().toString()
        )
    }

    private fun extractBankName(sender: String, message: String): String {
        val bankPatterns = listOf(
            "HSBC", "SBI", "ICICI", "HDFC", "Axis", "Kotak", "YES", "Union", "IDBI"
        )
        for (bank in bankPatterns) {
            if (message.contains(bank, ignoreCase = true)) {
                return "$bank Credit Card"
            }
        }
        return sender.uppercase().replace(Regex("[^A-Z]"), "") + " Credit Card"
    }

    private fun extractBankCode(sender: String): String {
        return sender.uppercase().replace(Regex("[^A-Z0-9]"), "").take(5)
    }
}

// Generic Debit Handler (fallback)
class GenericDebitHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return (message.contains("debited", ignoreCase = true) ||
                message.contains("spent", ignoreCase = true)) &&
                !message.contains("Credit Card", ignoreCase = true)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        val accountNumber = extractAccountNumber(message)
        val bankCode = extractBankCodeFromSender(sender)
        val date = extractDate(message)

        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            accountNumber = accountNumber,
            bankCode = bankCode,
            bankName = extractBankNameFromSender(sender),
            category = classifyMerchant(merchant),
            transactionType = TransactionType.DEBIT,
            transactionDate = date,
            referenceId = merchant.hashCode().toString()
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
