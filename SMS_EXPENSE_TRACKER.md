# Paisa — SMS-Driven Expense Tracker

A real-time Android expense tracker that automatically reads bank SMS messages and creates transaction records. Built with **Kotlin + Jetpack Compose** and **Room Database**.

## Key Features

✅ **Automatic SMS Detection** — Reads incoming bank SMS in real-time
✅ **Background Processing** — Transactions added instantly without user interaction
✅ **Real-time Display** — Transactions appear in the app immediately when open
✅ **Manual Entry** — Add cash expenses and other transactions manually
✅ **Transaction Details** — View full SMS and extracted data
✅ **Categorization** — Auto-categorizes by merchant (Groceries, Travel, etc.)
✅ **Monthly Stats** — See income vs. expense breakdown
✅ **Account Tracking** — Automatically detects and tracks multiple bank accounts

## Architecture

### Data Layer
- **Transaction.kt** — Entity for transaction records
- **Account.kt** — Entity for bank accounts
- **TransactionDao & AccountDao** — Room database access
- **AppDatabase** — Room database setup
- **TransactionRepository** — Central data access point

### Business Logic
- **SmsParser.kt** — Extracts amount, merchant, account number from SMS
- **SmsReceiver.kt** — BroadcastReceiver that intercepts incoming SMS

### UI Layer (Jetpack Compose)
- **MainViewModel.kt** — Manages UI state and month/search filtering
- **HomeScreen.kt** — Main transaction list with stats
- **TransactionDetailScreen.kt** — Full transaction view with SMS content
- **AddTransactionScreen.kt** — Manual transaction entry

### Key Services
- **MainActivity** — Activity entry point, permission handling, navigation

## Real-time Updates Architecture

```
Incoming SMS
    ↓
SmsReceiver (BroadcastReceiver)
    ↓
SmsParser extracts amount/merchant/account
    ↓
Database insert (Room)
    ↓
Flow<List<Transaction>> emits new data
    ↓
Compose recomposition (HomeScreen)
    ↓
User sees transaction instantly
```

## SMS Parsing

The parser extracts:
- **Amount**: Matches patterns like "INR 500.00", "Rs. 1000"
- **Merchant**: Looks for "to MERCHANT", "at MERCHANT", "towards MERCHANT"
- **Account Number**: Extracts XX3717 from "A/c XX3717"
- **Bank Code**: Identifies ICICI, HDFC, SBI, SLICE, PAYTM, etc.
- **Direction**: Debited or credited (income vs expense)

## Supported Banks (Auto-detected)

- ICICI Bank (VM-ICICIB)
- HDFC Bank (VM-HDFCBK)
- SBI (VK-SBIINB)
- Axis Bank (AD-AXISBK)
- Slice Bank (AD-SLICEB)
- Slice Credit Card (AD-SLICE)
- Paytm Wallet (VM-PYTMPB)
- And more (regex-based detection)

## Auto-categories

Transactions are classified based on merchant name:
- **Groceries**: Blinkit, Zepto, Instamart, Walmart, Amazon Fresh
- **Eating out**: Swiggy, Zomato, Uber Eats, Café, Restaurant
- **Travel**: Uber, Ola, Metro, Flight, Petrol, Hotel
- **Bills**: Airtel, Jio, Electricity, Water, Internet
- **Shopping**: Amazon, Flipkart, Myntra, EBay
- **Health**: Apollo, Medical, Hospital, Pharmacy
- **Rent**: Landlord, Lease
- **Salary**: NEFT, Credit (income)

## Permissions Required

```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

App requests these on startup and handles permission grants/denials.

## Database Schema

### Transactions Table
```sql
CREATE TABLE transactions (
    id TEXT PRIMARY KEY,
    merchant TEXT,
    amount REAL,
    category TEXT,
    accountId TEXT,
    timestamp LONG,
    source TEXT, -- "sms" or "manual"
    smsSender TEXT,
    smsContent TEXT,
    isIncome INTEGER
)
```

### Accounts Table
```sql
CREATE TABLE accounts (
    id TEXT PRIMARY KEY,
    name TEXT,
    shortName TEXT,
    bankCode TEXT,
    accountNumber TEXT,
    initialBalance REAL,
    color TEXT,
    isActive INTEGER
)
```

## State Management

**MainViewModel** manages:
- Current month (for filtering transactions)
- Search query
- Selected account filter
- Monthly stats calculation
- Transaction filtering

**Flow-based reactive updates**:
```kotlin
val transactions: Flow<List<Transaction>> = repository.getAllTransactions()
```

Changes to the database automatically trigger Compose recomposition.

## How to Use

### 1. Grant Permissions
On first launch, grant SMS and notification permissions when prompted.

### 2. Auto-detection
When a transaction SMS arrives, it's automatically:
- Parsed and validated
- Inserted into the database
- Displayed in the Home screen

### 3. View Transactions
- **Home**: See all transactions with monthly stats
- **Tap transaction**: View full details with original SMS
- **Month navigation**: Use ‹ › buttons to browse months

### 4. Add Manually
- Tap the "+" FAB button (when implemented)
- Select Spent/Received
- Enter amount and merchant
- Pick category and account
- Save

### 5. Search (when implemented)
- Type merchant name or category
- Results filter in real-time

## Integration Points

### Adding Initial Accounts
Create accounts in the database before SMS arrives:
```kotlin
val account = Account(
    id = UUID.randomUUID().toString(),
    name = "ICICI Bank ••3717",
    shortName = "ICICI ••3717",
    bankCode = "ICICI",
    accountNumber = "3717",
)
repository.insertAccount(account)
```

### Custom Categorization
Modify `SmsReceiver.classifyTransaction()` to add more patterns:
```kotlin
private fun classifyTransaction(merchant: String): String {
    val text = merchant.lowercase()
    return when {
        text.contains(Regex("pattern")) -> "Category"
        // ... more patterns
        else -> "Shopping"
    }
}
```

### SMS Filtering
Modify `SmsParser.isBankMessage()` to include more bank codes.

## Color Scheme

- **Primary**: #C67139 (Orange)
- **Surface**: #F5EAD8 (Cream)
- **Background**: #EFE3CD (Warm beige)
- **Text Primary**: #201E1D (Dark brown)
- **Text Secondary**: #645C50 (Medium brown)
- **Income**: #7A8A5E (Sage green)
- **Expense**: #8C491A (Rust)

## Future Enhancements

1. **Full Accounts Screen** — Manage accounts, view per-account stats
2. **Settings Screen** — Toggle SMS reading, manage categories, export CSV
3. **Search & Filter** — Comprehensive transaction search
4. **Notifications** — Toast alerts for new transactions
5. **Category Management** — Custom categories and rules
6. **Monthly Budget** — Set spending limits per category
7. **Charts** — Pie/bar charts for spending breakdown
8. **CSV Export** — Export monthly data
9. **Backup** — Cloud backup of transactions
10. **Multi-currency** — Support for different currencies

## Troubleshooting

### Transactions not appearing
1. Check if SMS permission is granted
2. Verify SMS sender is in the bank list (SmsParser.isBankMessage)
3. Check logcat: `adb logcat | grep SmsReceiver`
4. Ensure amount/merchant extraction regex works for your bank's format

### Duplicate transactions
- SMS may arrive on multiple devices
- Consider checking for duplicate amounts/timestamps before insert

### App crashes on startup
- Ensure Room annotations are correct
- Check that database migrations are set up if schema changes

## Testing

### Manual SMS Testing
Use Android Studio's emulator:
```
adb shell am startservice -a com.android.internal.telephony.SMS_RECEIVED \
  --es pdu "0031000B916105551234..."
```

Or use the emulator's Extended controls to send SMS.

### Database Inspection
Use Android Studio's Database Inspector to view Room tables in real-time.

## License

Built as a personal expense tracker. Modify as needed!
