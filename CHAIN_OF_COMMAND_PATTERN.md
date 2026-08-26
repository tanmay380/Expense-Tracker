# Chain of Command Pattern for SMS Parsing

This document explains the Chain of Command design pattern implementation for parsing bank SMS messages with different formats.

## Problem Statement

Different banks send SMS messages in completely different formats:

1. **Slice**: `Rs. 25 spent on your credit card xx2690 at Veer singh on 21-Aug-26`
2. **SBI**: `Rs.963.61 spent on your SBI Credit Card ending with 8622 at BharatConnectUtiliti on 20-08-26`
3. **HSBC**: `HSBC Credit Card xx4758 used at SWIGGY PVT LTD FOOD2 for INR 216.00 on 19/08/26`
4. **ICICI Card**: `INR 269.00 spent using ICICI Bank Card XX1005 on 18-Aug-26 on AMAZON PAY`
5. **ICICI Account**: `ICICI Bank Acc XX717 debited Rs. 20,000.34 on 18-Aug-26`

A simple regex parser can't handle all these variations efficiently.

## Solution: Chain of Command Pattern

The Chain of Command is a behavioral design pattern that lets you pass requests along a chain of handlers. Each handler decides either to process the request or pass it along the chain.

### Architecture

```
SMS Message + Sender
    ↓
ChainOfCommandParser
    ↓
SliceBankHandler ──(No)──→
    ↓ (Yes)
Parse Slice SMS
    ↓
Return ParsedTransaction

If No → Next Handler
    ↓
SBICardHandler
    ↓
HSBCCardHandler
    ↓
ICICICardHandler
    ↓
ICICIAccountHandler
    ↓
ICICISavingsAccountHandler
    ↓
GenericCreditCardHandler
    ↓
GenericDebitHandler (Final fallback)
```

## Key Components

### 1. **SmsHandler.kt** - Abstract Base Class

```kotlin
abstract class SmsHandler {
    protected var nextHandler: SmsHandler? = null

    fun chainWith(handler: SmsHandler): SmsHandler {
        this.nextHandler = handler
        return this
    }

    fun handle(sender: String, message: String): ParsedTransaction {
        return if (canHandle(sender, message)) {
            parse(sender, message)
        } else {
            nextHandler?.handle(sender, message) ?: ParsedTransaction(isValid = false)
        }
    }

    abstract fun canHandle(sender: String, message: String): Boolean
    abstract fun parse(sender: String, message: String): ParsedTransaction
}
```

**Responsibilities:**
- Maintain reference to next handler in chain
- Decide whether to handle or pass to next
- Provide utility methods for extracting data (amount, merchant, card number, etc.)
- Provide merchant classification

### 2. **BankHandlers.kt** - Concrete Implementations

Each handler extends `SmsHandler` and implements:

#### **SliceBankHandler**
```kotlin
class SliceBankHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("slice", ignoreCase = true) &&
            (message.contains("spent", ignoreCase = true) || ...)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        // Slice-specific parsing logic
    }
}
```

**Handles:** Slice Bank SMS messages  
**Pattern Match:** Sender contains "slice" AND message contains "spent/sent/debited"

#### **SBICardHandler**
```kotlin
class SBICardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("SBI", ignoreCase = true) &&
            message.contains("SBI Credit Card", ignoreCase = true) && ...
    }
}
```

**Handles:** SBI Credit Card transactions  
**Pattern Match:** "SBI Credit Card" in message

#### **HSBCCardHandler**
```kotlin
class HSBCCardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return message.contains("HSBC Credit Card", ignoreCase = true) &&
            (message.contains("used", ignoreCase = true) || ...)
    }
}
```

**Handles:** HSBC Credit Card transactions  
**Pattern Match:** "HSBC Credit Card" anywhere in message

#### **ICICICardHandler**
```kotlin
class ICICICardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("ICICI Bank Card", ignoreCase = true) && ...
    }
}
```

**Handles:** ICICI Bank Card debit transactions

#### **ICICIAccountHandler**
```kotlin
class ICICIAccountHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("ICICI Bank Acc", ignoreCase = true) && ...
    }
}
```

**Handles:** ICICI Account transfers/debits

#### **ICICISavingsAccountHandler**
```kotlin
class ICICISavingsAccountHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("ICICI", ignoreCase = true) &&
            message.contains("Savings Account", ignoreCase = true) && ...
    }
}
```

**Handles:** ICICI Savings Account transactions

#### **GenericCreditCardHandler**
```kotlin
class GenericCreditCardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return message.contains("Credit Card", ignoreCase = true) && ...
    }
}
```

**Handles:** Any credit card that has "Credit Card" in the message  
**Fallback:** Handles unknown banks' credit cards

#### **GenericDebitHandler**
```kotlin
class GenericDebitHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return (message.contains("debited", ignoreCase = true) || ...) &&
                !message.contains("Credit Card", ignoreCase = true)
    }
}
```

**Handles:** Any debit that doesn't match specific handlers  
**Final Fallback:** Catches anything not handled above

### 3. **ChainOfCommandParser.kt** - Chain Setup

```kotlin
class ChainOfCommandParser {
    init {
        val sliceHandler = SliceBankHandler()
        val sbiCardHandler = SBICardHandler()
        // ... create all handlers
        
        // Build chain (order matters!)
        sliceHandler.setNextHandler(sbiCardHandler)
        sbiCardHandler.setNextHandler(hsbcCardHandler)
        hsbcCardHandler.setNextHandler(iciciCardHandler)
        // ... etc
    }

    fun parse(sender: String, message: String): ParsedTransaction {
        return rootHandler.handle(sender, message)
    }
}
```

**Responsibility:** Set up and maintain the chain order

### 4. **SmsParserFactory.kt** - Singleton Access

```kotlin
object SmsParserFactory {
    private val parser = ChainOfCommandParser()

    fun parseTransaction(sender: String, message: String): ParsedTransaction {
        return parser.parse(sender, message)
    }
}
```

**Usage:**
```kotlin
val result = SmsParserFactory.parseTransaction(sender, messageBody)
if (result.isValid) {
    // Use result.amount, result.merchant, result.bankCode, etc.
}
```

## Chain Order (Critical!)

The order of handlers in the chain is crucial:

```
1. SliceBankHandler       (Most specific - Slice only)
2. SBICardHandler         (SBI Card specific)
3. HSBCCardHandler        (HSBC Card specific)
4. ICICICardHandler       (ICICI Card specific)
5. ICICIAccountHandler    (ICICI Account specific)
6. ICICISavingsHandler    (ICICI Savings specific)
7. GenericCreditCard      (Generic credit card fallback)
8. GenericDebit           (Final fallback)
```

**Why this order?**
- Most specific first: Slice, SBI, HSBC, ICICI handlers check for exact keywords
- Generic handlers last: They match broader patterns like "Credit Card" or "debited"
- If we put generic handlers first, they'd catch everything and specific handlers would never be reached

## Data Model

### ParsedTransaction

```kotlin
data class ParsedTransaction(
    val isValid: Boolean,                      // Whether parsing succeeded
    val amount: Double = 0.0,                  // Transaction amount
    val merchant: String = "",                 // Store/company name
    val cardNumber: String = "",               // Last 4 digits
    val accountNumber: String = "",            // Last 4 digits
    val bankCode: String = "",                 // Bank identifier
    val bankName: String = "",                 // Full bank name
    val category: String = "",                 // Auto-classified category
    val transactionType: TransactionType = DEBIT, // DEBIT, CREDIT, TRANSFER
    val transactionDate: String = "",          // Transaction date
    val referenceId: String = "",              // Ref/Trx ID
)

enum class TransactionType {
    DEBIT,      // Money out
    CREDIT,     // Money in
    TRANSFER    // Money transfer between accounts
}
```

## Shared Utility Methods

All handlers inherit these utilities:

```kotlin
protected fun extractAmount(message: String): Double?
protected fun extractMerchant(message: String): String
protected fun extractCardNumber(message: String): String
protected fun extractAccountNumber(message: String): String
protected fun extractReferenceId(message: String): String
protected fun extractDate(message: String): String
protected fun classifyMerchant(merchant: String): String  // Auto-categorize
```

## Usage in SmsReceiver

```kotlin
for (message in messages) {
    val sender = message.originatingAddress ?: continue
    val body = message.messageBody

    // Use chain of command
    val parsed = SmsParserFactory.parseTransaction(sender, body)

    if (!parsed.isValid) {
        Log.d(TAG, "Skipped non-bank SMS")
        continue
    }

    // Now parsed.bankCode, parsed.merchant, etc. are populated
    val transaction = Transaction(
        merchant = parsed.merchant,
        amount = if (parsed.transactionType == CREDIT) 
                    parsed.amount else -parsed.amount,
        category = parsed.category,
        // ... etc
    )
    repository.insertTransaction(transaction)
}
```

## Adding a New Bank

1. **Create a new handler** in `BankHandlers.kt`:

```kotlin
class MyBankCardHandler : SmsHandler() {
    override fun canHandle(sender: String, message: String): Boolean {
        return sender.contains("MYBANK", ignoreCase = true) &&
            message.contains("My Bank Card", ignoreCase = true)
    }

    override fun parse(sender: String, message: String): ParsedTransaction {
        val amount = extractAmount(message) ?: return ParsedTransaction(isValid = false)
        val merchant = extractMerchant(message)
        // ... extract other fields
        
        return ParsedTransaction(
            isValid = true,
            amount = amount,
            merchant = merchant,
            // ... populate other fields
            bankCode = "MYBANK",
            bankName = "My Bank Card"
        )
    }
}
```

2. **Insert into chain** in `ChainOfCommandParser.kt`:

```kotlin
val myBankHandler = MyBankCardHandler()
// Place it in the right position (usually after specific banks, before generics)
hsbcCardHandler.setNextHandler(myBankHandler)
myBankHandler.setNextHandler(iciciCardHandler)
```

## Benefits of Chain of Command

✅ **Separation of Concerns** — Each handler only knows about one bank format  
✅ **Open/Closed Principle** — Easy to add new banks without modifying existing code  
✅ **Single Responsibility** — Each handler has one job  
✅ **Testable** — Test each handler independently  
✅ **Maintainable** — Changes to one bank don't affect others  
✅ **Extensible** — Easy to add bank-specific logic later  

## Example: Parsing Your SMS Messages

### Message 1: Slice
```
Input: 
  Sender: "slice"
  Message: "Rs. 25 spent on your credit card xx2690 at Veer singh on 21-Aug-26"

Chain:
  SliceBankHandler.canHandle() → YES ✓
  SliceBankHandler.parse() → ParsedTransaction(
    amount = 25.0,
    merchant = "Veer singh",
    cardNumber = "2690",
    bankCode = "SLICE",
    category = "Shopping"
  )
```

### Message 2: SBI
```
Input:
  Sender: "SBI"
  Message: "Rs.963.61 spent on your SBI Credit Card ending with 8622 at BharatConnectUtiliti on 20-08-26"

Chain:
  SliceBankHandler.canHandle() → NO ✗
  SBICardHandler.canHandle() → YES ✓
  SBICardHandler.parse() → ParsedTransaction(
    amount = 963.61,
    merchant = "BharatConnectUtiliti",
    cardNumber = "8622",
    bankCode = "SBI",
    category = "Bills"
  )
```

### Message 5: ICICI Account
```
Input:
  Sender: "ICICI"
  Message: "ICICI Bank Acc XX717 debited Rs. 20,000.34 on 18-Aug-26 InfoNRS*USD205.81"

Chain:
  SliceBankHandler.canHandle() → NO ✗
  SBICardHandler.canHandle() → NO ✗
  HSBCCardHandler.canHandle() → NO ✗
  ICICICardHandler.canHandle() → NO ✗
  ICICIAccountHandler.canHandle() → YES ✓
  ICICIAccountHandler.parse() → ParsedTransaction(
    amount = 20000.34,
    merchant = "Bank Transfer",
    accountNumber = "717",
    bankCode = "ICICI",
    category = "Shopping"
  )
```

## Testing Strategy

### Test SliceBankHandler
```kotlin
fun testSliceSpent() {
    val handler = SliceBankHandler()
    val result = handler.handle(
        "slice",
        "Rs. 25 spent on your credit card xx2690 at Veer singh on 21-Aug-26"
    )
    assert(result.isValid)
    assert(result.amount == 25.0)
    assert(result.merchant == "Veer singh")
}
```

### Test Chain Order
```kotlin
fun testChainProcessesInOrder() {
    val parser = ChainOfCommandParser()
    
    // SBI message should be handled by SBICardHandler, not SliceHandler
    val result = parser.parse("SBI", sbiMessage)
    assert(result.bankCode == "SBI")
    assert(result.bankName == "SBI Credit Card")
}
```

## Future Enhancements

1. **Priority Handlers** — Add handler priority instead of strict order
2. **Regex Registry** — Dynamically register patterns per bank
3. **Machine Learning** — Train a model to classify SMS type
4. **Caching** — Cache parsed results by checksum
5. **Async Parsing** — Non-blocking handler chain
6. **Validation** — Cross-validate parsed data against bank websites

---

This pattern provides a robust, maintainable solution for parsing diverse SMS formats!
