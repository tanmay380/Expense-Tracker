package com.example.expensetracker.util

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.data.TransactionRepository
import com.example.sms_parser.factory.SmsParserFactory
import com.example.sms_parser.data.TransactionType
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

                val factory = SmsParserFactory.getInstance()
                if (!factory.isBankMessage(sender, body)) continue

                val parsed = factory.parse(sender, body)
                if (!parsed.isValid) continue

                // Check if transaction already exists (by amount, merchant, timestamp)
                val txns = repository.searchTransactions("${parsed.merchant}").first()
                val existing = txns.any {
                    it.merchant == parsed.merchant &&
                    kotlin.math.abs(it.timestamp - date) < 60000 // 1 minute window
                }

                if (existing) continue

                // Get or create account
                val accountIdentifier = if (parsed.cardNumber.isNotEmpty()) {
                    "${parsed.bankCode}_CARD_${parsed.cardNumber}"
                } else {
                    "${parsed.bankCode}_ACC_${parsed.accountNumber}"
                }

                var account = repository.getAccountByBankCode(accountIdentifier)
                if (account == null) {
                    val number = parsed.cardNumber.ifEmpty { parsed.accountNumber }
                    account = Account(
                        id = UUID.randomUUID().toString(),
                        name = "${parsed.bankName} ••$number",
                        shortName = "${parsed.bankCode} ••$number",
                        bankCode = accountIdentifier,
                        accountNumber = number,
                    )
                    repository.insertAccount(account)
                }

                val isIncome = parsed.transactionType == TransactionType.CREDIT
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    merchant = parsed.merchant,
                    amount = if (isIncome) parsed.amount else -parsed.amount,
                    category = parsed.category,
                    accountId = account.id,
                    timestamp = date,
                    source = "sms",
                    smsSender = sender,
                    smsContent = body,
                    isIncome = isIncome,
                )

                repository.insertTransaction(transaction)
                processedCount++
                Log.d(TAG, "Scanned: ${parsed.merchant} - ${parsed.amount}")
            }
        }

        return processedCount
    }
}
