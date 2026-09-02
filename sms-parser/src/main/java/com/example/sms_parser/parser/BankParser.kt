package com.example.sms_parser.parser

import com.example.sms_parser.data.ParsedTransaction

interface BankParser {
    fun canHandle(sender: String, message: String): Boolean
    fun parse(sender: String, message: String): ParsedTransaction
}
