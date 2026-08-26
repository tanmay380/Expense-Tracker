package com.example.expensetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.util.SmsParserFactory
import com.example.expensetracker.util.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.log

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "tanmay"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "==== SMS RECEIVED ====")
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "Wrong action, ignoring")
            return
        }
        if (context == null) {
            Log.d(TAG, "Context is null, ignoring")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullMessage = StringBuilder()
        var sender = ""

        for (sms in messages) {
            sender = sms.displayOriginatingAddress ?: ""
            fullMessage.append(sms.displayMessageBody)
            Log.d(TAG, "SMS Part from $sender: ${sms.displayMessageBody}")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting background processing...")
                val db = AppDatabase.getDatabase(context)
                val repository = com.example.expensetracker.data.TransactionRepository(
                    db.transactionDao(),
                    db.accountDao()
                )

                val senderFinal = sender
                if (senderFinal.isEmpty()) {
                    Log.d(TAG, "No sender, returning")
                    return@launch
                }

                val body = fullMessage.toString()
                Log.d(TAG, "Full SMS body: $body")

                // Parse using Chain of Command pattern
                val parsed = SmsParserFactory.parseTransaction(senderFinal, body)
                Log.d(TAG, "Parse result: isValid=${parsed.isValid}, bankCode=${parsed.bankCode}, merchant=${parsed.merchant}, amount=${parsed.amount}")

                if (!parsed.isValid || parsed.bankCode.isEmpty()) {
                    Log.d(TAG, "Invalid or non-bank SMS, skipping")
                    return@launch
                }

                // Get or create account
                val accountIdentifier = if (parsed.cardNumber.isNotEmpty()) {
                    "${parsed.bankCode}_CARD_${parsed.cardNumber}"
                } else {
                    "${parsed.bankCode}_ACC_${parsed.accountNumber}"
                }

                var account = repository.getAccountByBankCode(accountIdentifier)
                if (account == null) {
                    val number = parsed.cardNumber.ifEmpty { parsed.accountNumber }
                    account = com.example.expensetracker.data.Account(
                        id = UUID.randomUUID().toString(),
                        name = "${parsed.bankName} ••$number",
                        shortName = "${parsed.bankCode} ••$number",
                        bankCode = accountIdentifier,
                        accountNumber = number,
                    )
                    repository.insertAccount(account)
                    Log.d(TAG, "Created account: ${account.name}")
                }

                // Determine if income or expense
                val isIncome = parsed.transactionType == TransactionType.CREDIT
                val amount = if (isIncome) parsed.amount else -parsed.amount

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    merchant = parsed.merchant,
                    amount = amount,
                    category = parsed.category,
                    accountId = account.id,
                    timestamp = System.currentTimeMillis(),
                    source = "sms",
                    smsSender = senderFinal,
                    smsContent = body,
                    isIncome = isIncome,
                )

                Log.d(TAG, "About to insert transaction: $transaction")
                repository.insertTransaction(transaction)
                Log.d(TAG, "✅ TRANSACTION INSERTED: ${parsed.merchant} - ₹${parsed.amount}")
                Log.d(TAG, "==== SMS COMPLETE ====")

            } catch (e: Exception) {
                Log.e(TAG, "ERROR processing SMS", e)
                e.printStackTrace()
            }
        }
    }
}
