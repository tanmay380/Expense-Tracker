package com.example.expensetracker.util

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.data.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

object SmsReader {
    private const val TAG = "tanmay"

    suspend fun scanAllMessages(
        context: Context,
        repository: TransactionRepository
    ): Int {
        var processedCount = 0
        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use { c ->
            val senderIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)

            while (c.moveToNext()) {
                val sender = c.getString(senderIndex)
                val body = c.getString(bodyIndex)
                val date = c.getLong(dateIndex)

                if (!SmsParser.isBankMessage(sender)) continue

                val parsed = SmsParser.parseTransaction(body, sender) ?: continue

                // Check if transaction already exists (by amount, merchant, timestamp)
                val txns = repository.searchTransactions("${parsed.merchant}").first()
                val existing = txns.any {
                    it.merchant == parsed.merchant &&
                    kotlin.math.abs(it.timestamp - date) < 60000 // 1 minute window
                }

                if (existing) continue

                // Get or create account
                var account = repository.getAccountByBankCode(parsed.bankCode)
                if (account == null) {
                    account = Account(
                        id = UUID.randomUUID().toString(),
                        name = "${parsed.bankCode} Bank ••${parsed.accountNumber}",
                        shortName = "${parsed.bankCode} ••${parsed.accountNumber}",
                        bankCode = parsed.bankCode,
                        accountNumber = parsed.accountNumber,
                    )
                    repository.insertAccount(account)
                }

                val category = classifyTransaction(parsed.merchant)
                val transaction = Transaction(
                    id = SmsParser.generateTransactionId(),
                    merchant = parsed.merchant,
                    amount = if (parsed.isIncome) parsed.amount else -parsed.amount,
                    category = category,
                    accountId = account.id,
                    timestamp = date,
                    source = "sms",
                    smsSender = sender,
                    smsContent = body,
                    isIncome = parsed.isIncome,
                )

                repository.insertTransaction(transaction)
                processedCount++
                Log.d(TAG, "Scanned: ${parsed.merchant} - ${parsed.amount}")
            }
        }

        return processedCount
    }

    private fun classifyTransaction(merchant: String): String {
        val text = merchant.lowercase()
        return when {
            text.contains(Regex("(blinkit|zepto|instamart|grocery|walmart|amazon fresh)")) -> "Groceries"
            text.contains(Regex("(swiggy|zomato|uber eats|food|restaurant|cafe|coffee)")) -> "Eating out"
            text.contains(Regex("(uber|ola|metro|rail|bus|flight|taxi|travel|petrol|fuel|hotel)")) -> "Travel"
            text.contains(Regex("(airtel|jio|vodafone|electricity|water|gas|internet|phone|bills)")) -> "Bills"
            text.contains(Regex("(amazon|flipkart|ebay|myntra|shopping|mall)")) -> "Shopping"
            text.contains(Regex("(apollo|medical|doctor|hospital|pharmacy|health|fitness)")) -> "Health"
            text.contains(Regex("(rent|landlord|lease)")) -> "Rent"
            text.contains(Regex("(salary|neft|credit)")) -> "Salary"
            else -> "Shopping"
        }
    }
}
