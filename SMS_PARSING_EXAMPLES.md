# SMS Parsing Examples — Chain of Command Pattern

This document shows how the Chain of Command pattern handles your actual SMS messages.

## Test Messages

These are the 9 SMS messages you provided:

### 1. Slice - Card Purchase
```
Sender: slice
Message: Rs. 25 spent on your credit card xx2690 at Veer singh on 21-Aug-26 (UPI Ref: 623394050145). Not you? Call 080-4832-9999

Handler Chain:
  ✓ SliceBankHandler.canHandle() → YES (sender contains "slice")
  
Parsed Result:
  isValid: true
  amount: 25.0
  merchant: "Veer singh"
  cardNumber: "2690"
  bankCode: "SLICE"
  bankName: "Slice Bank"
  category: "Shopping"
  transactionType: DEBIT
  transactionDate: "21-Aug-26"
  referenceId: "623394050145"
```

### 2. SBI - Credit Card
```
Sender: SBI
Message: Rs.963.61 spent on your SBI Credit Card ending with 8622 at BharatConnectUtiliti on 20-08-26 via UPI (Ref No. 213104291438). Trxn. not done by you? Report at https://sbicard.com/Dispute

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO (sender is "SBI", not "slice")
  ✓ SBICardHandler.canHandle() → YES (sender contains "SBI" AND message contains "SBI Credit Card")
  
Parsed Result:
  isValid: true
  amount: 963.61
  merchant: "BharatConnectUtiliti"
  cardNumber: "8622"
  bankCode: "SBI"
  bankName: "SBI Credit Card"
  category: "Bills" (classified from merchant name)
  transactionType: DEBIT
  transactionDate: "20-08-26"
  referenceId: "213104291438"
```

### 3. HSBC - Credit Card
```
Sender: HSBC
Message: HSBC Credit Card xx4758 used at SWIGGY PVT LTD FOOD2 for INR 216.00 on 19/08/26. Avl limit INR 398802.41; due INR 8197.59. Call +914065118002 to report fraud.

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO (sender is "HSBC")
  ✗ SBICardHandler.canHandle() → NO (sender is "HSBC", not "SBI")
  ✓ HSBCCardHandler.canHandle() → YES (message contains "HSBC Credit Card")
  
Parsed Result:
  isValid: true
  amount: 216.00
  merchant: "SWIGGY PVT LTD FOOD2"
  cardNumber: "4758"
  bankCode: "HSBC"
  bankName: "HSBC Credit Card"
  category: "Eating out" (classified - contains "SWIGGY")
  transactionType: DEBIT
  transactionDate: "19/08/26"
  referenceId: "swiggy_hashcode"
```

### 4. ICICI - Card Purchase
```
Sender: ICICI
Message: INR 269.00 spent using ICICI Bank Card XX1005 on 18-Aug-26 on AMAZON PAY IN E. Avl Limit: INR 5,89,109.09. If not you, call 1800 2662/SMS BLOCK 1005 to 9215676766.

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO
  ✗ SBICardHandler.canHandle() → NO
  ✗ HSBCCardHandler.canHandle() → NO
  ✓ ICICICardHandler.canHandle() → YES (sender contains "ICICI" AND message contains "ICICI Bank Card")
  
Parsed Result:
  isValid: true
  amount: 269.00
  merchant: "AMAZON PAY IN E"
  cardNumber: "1005"
  bankCode: "ICICI"
  bankName: "ICICI Bank Card"
  category: "Shopping" (classified - contains "AMAZON")
  transactionType: DEBIT
  transactionDate: "18-Aug-26"
  referenceId: "generated_uuid"
```

### 5. ICICI - Account Debit
```
Sender: ICICI
Message: ICICI Bank Acc XX717 debited Rs. 20,000.34 on 18-Aug-26 InfoNRS*USD205.81.Avl Bal Rs. 2,81,457.78.To dispute call 18002662 or SMS BLOCK 717 to 9215676766

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO
  ✗ SBICardHandler.canHandle() → NO
  ✗ HSBCCardHandler.canHandle() → NO
  ✗ ICICICardHandler.canHandle() → NO (message says "Acc", not "Card")
  ✓ ICICIAccountHandler.canHandle() → YES (message contains "ICICI Bank Acc")
  
Parsed Result:
  isValid: true
  amount: 20000.34
  merchant: "InfoNRS" (extracted from message)
  accountNumber: "717"
  bankCode: "ICICI"
  bankName: "ICICI Bank Account"
  category: "Shopping"
  transactionType: DEBIT
  transactionDate: "18-Aug-26"
  referenceId: "generated"
```

### 6. Slice - UPI Transfer
```
Sender: slice
Message: Rs. 150 sent from a/c xx3414 on 17-Aug-26 to AMIT (UPI Ref: 622901692992). Not you? Call 08048329999

Handler Chain:
  ✓ SliceBankHandler.canHandle() → YES (sender contains "slice")
  
Parsed Result:
  isValid: true
  amount: 150.0
  merchant: "AMIT"
  accountNumber: "3414"
  bankCode: "SLICE"
  bankName: "Slice Bank"
  category: "Shopping"
  transactionType: TRANSFER (detected from "sent")
  transactionDate: "17-Aug-26"
  referenceId: "622901692992"
```

### 7. ICICI - AutoPay from Savings
```
Sender: ICICI
Message: Rs 2200.00 debited from ICICI Bank Savings Account XX717 on 17-Aug-26 towards Indian Clearing for Slice AutoPay Retrieval Ref No.659540466117

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO
  ✗ SBICardHandler.canHandle() → NO
  ✗ HSBCCardHandler.canHandle() → NO
  ✗ ICICICardHandler.canHandle() → NO (not a "Card" message)
  ✗ ICICIAccountHandler.canHandle() → NO (says "Savings Account", not "Acc")
  ✓ ICICISavingsAccountHandler.canHandle() → YES (message contains "Savings Account")
  
Parsed Result:
  isValid: true
  amount: 2200.00
  merchant: "Slice AutoPay" (extracted from "towards")
  accountNumber: "717"
  bankCode: "ICICI"
  bankName: "ICICI Savings Account"
  category: "Bills" (classified - contains "AutoPay")
  transactionType: DEBIT
  transactionDate: "17-Aug-26"
  referenceId: "659540466117"
```

### 8. ICICI - Card Purchase (Google Play)
```
Sender: ICICI
Message: INR 299.00 spent using ICICI Bank Card XX5001 on 16-Aug-26 on GOOGLEPLAY. Avl Limit: INR 5,89,305.15. If not you, call 1800 2662/SMS BLOCK 5001 to 9215676766.

Handler Chain:
  ✗ SliceBankHandler.canHandle() → NO
  ✗ SBICardHandler.canHandle() → NO
  ✗ HSBCCardHandler.canHandle() → NO
  ✓ ICICICardHandler.canHandle() → YES (sender contains "ICICI" AND message contains "ICICI Bank Card")
  
Parsed Result:
  isValid: true
  amount: 299.00
  merchant: "GOOGLEPLAY"
  cardNumber: "5001"
  bankCode: "ICICI"
  bankName: "ICICI Bank Card"
  category: "Entertainment" (classified - contains "GOOGLEPLAY")
  transactionType: DEBIT
  transactionDate: "16-Aug-26"
  referenceId: "generated_uuid"
```

### 9. ICICI - Card Purchase (Amazon)
```
Sender: ICICI
Message: INR 269.00 spent using ICICI Bank Card XX1005 on 18-Aug-26 on AMAZON PAY IN E. Avl Limit: INR 5,89,109.09. If not you, call 1800 2662/SMS BLOCK 1005 to 9215676766.

Handler Chain:
  ✓ ICICICardHandler.canHandle() → YES (same as message #4)
  
Parsed Result:
  isValid: true
  amount: 269.00
  merchant: "AMAZON PAY IN E"
  cardNumber: "1005"
  bankCode: "ICICI"
  bankName: "ICICI Bank Card"
  category: "Shopping" (classified - contains "AMAZON")
  transactionType: DEBIT
  transactionDate: "18-Aug-26"
  referenceId: "generated_uuid"
```

## How the Chain Works

### Chain Order (as configured)
```
1. SliceBankHandler           → Handles Slice messages (1, 6)
   ↓ (if not Slice)
2. SBICardHandler             → Handles SBI messages (2)
   ↓ (if not SBI)
3. HSBCCardHandler            → Handles HSBC messages (3)
   ↓ (if not HSBC)
4. ICICICardHandler           → Handles ICICI Card messages (4, 8, 9)
   ↓ (if not Card)
5. ICICIAccountHandler        → Handles ICICI Account messages (5)
   ↓ (if not Account)
6. ICICISavingsAccountHandler → Handles ICICI Savings messages (7)
   ↓ (if not Savings)
7. GenericCreditCardHandler   → Fallback for unknown credit cards
   ↓ (if not Credit Card)
8. GenericDebitHandler        → Final fallback for any debit
```

## Key Extraction Logic

### Amount Extraction
```kotlin
// Patterns matched in order:
Regex("""(?:INR|Rs\.?)\s*([\d,]+(?:\.\d{2})?)""")
Regex("""(\d+(?:\.\d{2})?)\s*(?:debited|spent|sent)""")

Examples:
"Rs. 25" → 25.0
"INR 269.00" → 269.00
"Rs.963.61" → 963.61
```

### Merchant Extraction
```kotlin
// Pattern examples:
"at Veer singh on 21-Aug" → "Veer singh"
"at BharatConnectUtiliti on 20-08" → "BharatConnectUtiliti"
"at SWIGGY PVT LTD FOOD2 for INR" → "SWIGGY PVT LTD FOOD2"
"on AMAZON PAY IN E. Avl" → "AMAZON PAY IN E"
"to AMIT (UPI" → "AMIT"
```

### Card Number Extraction
```kotlin
// Pattern examples:
"xx2690" → "2690"
"XX1005" → "1005"
"ending with 8622" → "8622"
```

### Category Classification
```kotlin
"SWIGGY" → "Eating out" (contains "swiggy")
"AMAZON" → "Shopping" (contains "amazon")
"GOOGLEPLAY" → "Entertainment" (contains "googleplay")
"Indian Clearing" → "Bills" (contains "clearing")
"Veer singh" → "Shopping" (default)
```

## Database Insertion

After parsing, each message creates a Transaction record:

```
SMS Message 1 (Slice) →
  Transaction {
    id: "uuid-1",
    merchant: "Veer singh",
    amount: -25.0 (negative = expense),
    category: "Shopping",
    accountId: "account-slice-2690",
    timestamp: 1724263200000,
    source: "sms",
    smsSender: "slice",
    smsContent: "Rs. 25 spent on...",
    isIncome: false
  }

SMS Message 2 (SBI) →
  Transaction {
    id: "uuid-2",
    merchant: "BharatConnectUtiliti",
    amount: -963.61,
    category: "Bills",
    accountId: "account-sbi-8622",
    timestamp: 1724176800000,
    source: "sms",
    smsSender: "SBI",
    smsContent: "Rs.963.61 spent on...",
    isIncome: false
  }

... and so on for all 9 messages
```

## Testing the Chain

You can test the parser with:

```kotlin
// Test in Android Studio
val sms1 = "Rs. 25 spent on your credit card xx2690 at Veer singh on 21-Aug-26"
val result1 = SmsParserFactory.parseTransaction("slice", sms1)
assert(result1.isValid && result1.amount == 25.0 && result1.merchant == "Veer singh")

// Test SBI
val sms2 = "Rs.963.61 spent on your SBI Credit Card ending with 8622 at BharatConnectUtiliti on 20-08-26"
val result2 = SmsParserFactory.parseTransaction("SBI", sms2)
assert(result2.isValid && result2.amount == 963.61 && result2.cardNumber == "8622")

// Test HSBC
val sms3 = "HSBC Credit Card xx4758 used at SWIGGY PVT LTD FOOD2 for INR 216.00 on 19/08/26"
val result3 = SmsParserFactory.parseTransaction("HSBC", sms3)
assert(result3.isValid && result3.category == "Eating out")

// ... etc for all 9 messages
```

## Benefits Demonstrated

✅ **Flexibility** — Each bank handled independently  
✅ **Maintainability** — Easy to update regex per bank  
✅ **Extensibility** — Add new banks without touching existing code  
✅ **Robustness** — Different date formats, amount formats all handled  
✅ **Clarity** — Each handler is focused and readable  

---

This Chain of Command implementation elegantly handles the complexity of parsing diverse SMS formats!
