package com.example.sms_parser.factory

import com.example.sms_parser.data.ParsedTransaction
import com.example.sms_parser.parser.BankParser
import com.example.sms_parser.parser.bank.AxisParser
import com.example.sms_parser.parser.bank.GenericCreditCardParser
import com.example.sms_parser.parser.bank.GenericDebitParser
import com.example.sms_parser.parser.bank.HDFCParser
import com.example.sms_parser.parser.bank.HSBCCardParser
import com.example.sms_parser.parser.bank.ICICIParser
import com.example.sms_parser.parser.bank.SBIParser
import com.example.sms_parser.parser.bank.SliceCardParser

class SmsParserFactory private constructor() {
    private val parsers: List<BankParser> = listOf(
        SliceCardParser(),
        ICICIParser(),
        SBIParser(),
        HDFCParser(),
        AxisParser(),
        HSBCCardParser(),
        GenericCreditCardParser(),
        GenericDebitParser()
    )

    fun parse(sender: String, message: String): ParsedTransaction {
        for (parser in parsers) {
            if (parser.canHandle(sender, message)) {
                return parser.parse(sender, message)
            }
        }
        return ParsedTransaction(isValid = false)
    }

    fun isBankMessage(sender: String, message: String): Boolean {
        val result = parse(sender, message)
        return result.isValid && result.bankCode.isNotEmpty()
    }

    companion object {
        private var instance: SmsParserFactory? = null

        fun getInstance(): SmsParserFactory {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SmsParserFactory()
                    }
                }
            }
            return instance!!
        }
    }
}
