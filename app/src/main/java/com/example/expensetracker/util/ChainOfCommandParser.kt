package com.example.expensetracker.util

/**
 * ChainOfCommandParser sets up and manages the chain of handlers
 * for parsing different bank SMS messages.
 *
 * The chain is ordered by specificity:
 * 1. Slice Bank (most specific)
 * 2. SBI Card
 * 3. HSBC Card
 * 4. ICICI Card
 * 5. ICICI Account
 * 6. ICICI Savings Account
 * 7. Generic Credit Card (fallback)
 * 8. Generic Debit (final fallback)
 */
class ChainOfCommandParser {
    private val rootHandler: SmsHandler

    init {
        // Build the chain from most specific to most generic
        val sliceHandler = SliceBankHandler()
        val sbiCardHandler = SBICardHandler()
        val hsbcCardHandler = HSBCCardHandler()
        val iciciCardHandler = ICICICardHandler()
        val iciciAccountHandler = ICICIAccountHandler()
        val iciciSavingsHandler = ICICISavingsAccountHandler()
        val genericCardHandler = GenericCreditCardHandler()
        val genericDebitHandler = GenericDebitHandler()

        // Link the chain
        sliceHandler.chainWith(sbiCardHandler)
        sbiCardHandler.chainWith(hsbcCardHandler)
        hsbcCardHandler.chainWith(iciciCardHandler)
        iciciCardHandler.chainWith(iciciAccountHandler)
        iciciAccountHandler.chainWith(iciciSavingsHandler)
        iciciSavingsHandler.chainWith(genericCardHandler)
        genericCardHandler.chainWith(genericDebitHandler)

        rootHandler = sliceHandler
    }

    fun parse(sender: String, message: String): ParsedTransaction {
        return rootHandler.handle(sender, message)
    }

    fun isBankMessage(sender: String, message: String): Boolean {
        val result = rootHandler.handle(sender, message)
        return result.isValid && result.bankCode.isNotEmpty()
    }
}

/**
 * Singleton instance of the parser
 */
object SmsParserFactory {
    private val parser = ChainOfCommandParser()

    fun parseTransaction(sender: String, message: String): ParsedTransaction {
        return parser.parse(sender, message)
    }

    fun isBankMessage(sender: String, message: String): Boolean {
        return parser.isBankMessage(sender, message)
    }
}
