# SMS Expense Tracker — Setup & Implementation Guide

## Project Structure

```
app/src/main/
├── java/com/example/expensetracker/
│   ├── MainActivity.kt                    # Entry point, permission handling, navigation
│   ├── data/
│   │   ├── Transaction.kt                 # Transaction entity
│   │   ├── Account.kt                     # Account entity
│   │   ├── TransactionDao.kt              # Database operations
│   │   ├── AppDatabase.kt                 # Room database
│   │   └── Repository.kt                  # Data access layer
│   ├── receiver/
│   │   └── SmsReceiver.kt                 # BroadcastReceiver for incoming SMS
│   ├── ui/
│   │   ├── MainViewModel.kt               # State management
│   │   ├── UiUtils.kt                     # Formatting & colors
│   │   └── screens/
│   │       ├── HomeScreen.kt              # Main transaction list
│   │       ├── TransactionDetailScreen.kt # Transaction details
│   │       └── AddTransactionScreen.kt    # Manual entry
│   └── util/
│       ├── SmsParser.kt                   # SMS parsing logic
│       └── SmsReader.kt                   # Bulk SMS scanning
├── res/
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Quick Start

### 1. Build and Run
```bash
# From project root
./gradlew build
./gradlew installDebug

# Or use Android Studio: Run > Run 'app'
```

### 2. Grant Permissions
When app starts, you'll see permission prompts:
- ✅ Read SMS messages
- ✅ Receive SMS notifications
- ✅ Post notifications (Android 13+)

Grant all three for full functionality.

### 3. Test SMS Reception
Use Android Emulator's extended controls or adb:
```bash
adb shell am startservice -a android.provider.Telephony.SMS_RECEIVED \
  -e pdu "0031000B916105551234F01801D40C8B6BA1B"
```

Or use emulator GUI:
1. Open emulator Extended Controls (right panel)
2. Go to Phone section
3. Send SMS with text like:
   > INR 500.00 debited from A/c XX3717 on 24-Aug-26 to BLINKIT. Avl bal INR 10000

## How It Works

### Real-time Transaction Flow

```
┌─────────────────────────────────────────┐
│ Bank sends SMS to user's phone          │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Android receives SMS broadcast          │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ SmsReceiver.onReceive() called           │
│ (runs on main thread, quick!)           │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Moves to Coroutine (IO dispatcher)      │
│ - Parse SMS with SmsParser              │
│ - Check if bank message                 │
│ - Extract amount, merchant, account     │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Get or create Account in Room DB        │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Insert Transaction into Room DB         │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Flow<List<Transaction>> emits           │
│ new data automatically                  │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ HomeScreen collects Flow data           │
│ (if app is open)                        │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Compose recomposes HomeScreen           │
│ User sees transaction instantly         │
└─────────────────────────────────────────┘
```

**Key points:**
- SMS parsing happens in background (IO dispatcher)
- No UI blocking
- If app is closed, transaction is saved silently
- When app opens, all saved transactions display immediately
- Real-time if app is open when SMS arrives

### Database Layer

**Transaction.kt** defines the structure:
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = "",      // UUID
    val merchant: String = "",             // Extracted: "BLINKIT"
    val amount: Double = 0.0,              // Negative = expense, positive = income
    val category: String = "Shopping",     // Auto-classified
    val accountId: String = "",            // Link to Account
    val timestamp: Long = 0L,              // When transaction occurred
    val source: String = "sms",            // "sms" or "manual"
    val smsSender: String? = null,         // SMS sender ID (e.g., "AD-SLICE")
    val smsContent: String? = null,        // Full SMS text
    val isIncome: Boolean = false,         // Income vs expense
)
```

**TransactionDao** provides queries:
```kotlin
// Get all transactions
fun getAllTransactions(): Flow<List<Transaction>>

// Search by merchant/category
fun searchTransactions(query: String): Flow<List<Transaction>>

// Filter by date range (for month view)
fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>>
```

### State Management

**MainViewModel** handles UI state:
```kotlin
class MainViewModel(val repository: TransactionRepository) : ViewModel() {
    // Current month for filtering
    val selectedMonth: StateFlow<YearMonth>
    
    // Search query
    val searchQuery: StateFlow<String>
    
    // Filtered transactions
    fun getFilteredTransactions(transactions: List<Transaction>): List<Transaction>
    
    // Monthly stats
    fun calculateMonthlyStats(transactions: List<Transaction>): MonthlyStats
}
```

**Compose automatically reacts to State changes:**
```kotlin
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    
    // When viewModel.transactions emits new data, HomeScreen recomposes automatically
}
```

## SMS Parser Details

### What Gets Extracted

From SMS: `"INR 842.00 debited from A/c XX3717 on 24-Aug-26 to BLINKIT"`

- **Amount**: 842.00 (regex: `INR\s*([0-9,]+\.?[0-9]*)`)
- **Account**: 3717 (regex: `XX([0-9]{4})`)
- **Merchant**: BLINKIT (regex: `to\s+([A-Z\s]+)`)
- **Direction**: debited = expense (regex: `debit|spent|paid`)
- **Bank Code**: Extracted from sender ID

### Supported Patterns

```kotlin
// Amount: INR 500, Rs. 1000, Rs.500.00
// Account: A/c XX3717, Card XX5678, Account XX1234
// Merchant: to BLINKIT, at ZEPTO, towards AIRTEL
// Direction: debited/spent/paid = expense; credited/received = income
```

### Adding Your Bank

If your bank's SMS doesn't parse, update regex patterns:

**In SmsParser.kt:**
```kotlin
private val amountPattern = Regex("""(?:INR|Rs\.?)\s*([0-9,]+(?:\.[0-9]{2})?)""")
```

Add your bank's amount format if it uses different wording.

**In SmsReceiver.kt:**
```kotlin
private fun extractBankCode(sender: String): String {
    return when {
        sender.contains("YOURBANK", ignoreCase = true) -> "YOURBANK"
        // ... existing banks
        else -> sender.uppercase()
    }
}
```

## Testing Scenarios

### Scenario 1: Receive SMS while app is closed
1. Close the app
2. Send SMS (via emulator or real SMS)
3. SmsReceiver.onReceive() processes it silently
4. Transaction added to database
5. Open app → transaction appears

### Scenario 2: Receive SMS while app is open
1. App is open on Home screen
2. SMS arrives
3. SmsReceiver processes in background
4. Flow emits new list
5. Compose recomposes instantly
6. User sees transaction appear

### Scenario 3: Manual transaction entry
1. Tap "+" button (when implemented)
2. Fill amount, merchant, category, account
3. Tap "Save it"
4. Transaction added to DB
5. Home screen recomposes with new transaction

### Scenario 4: Month navigation
1. Home screen shows "August 2026"
2. Tap "‹" button
3. viewModel.previousMonth() updates state
4. selectedMonth changes
5. Compose recomposes with previous month's transactions

## Customization

### Change Color Scheme

Edit `AppColors` in **UiUtils.kt**:
```kotlin
object AppColors {
    val Primary = Color(0xFFC67139)        // Change this
    val Surface = Color(0xFFF5EAD8)        // And this
    // ... etc
}
```

### Modify Category Icons/Colors

In **UiUtils.kt**:
```kotlin
fun getColorForCategory(category: String): Color {
    return when (category) {
        "Groceries" -> Color(0xFF7A8A5E)   // Your color
        // ... add more
    }
}
```

### Add More Categories

In **SmsReceiver.kt** or **AddTransactionScreen.kt**:
```kotlin
val categories = listOf("Groceries", "Eating out", "YOUR_CATEGORY", ...)
```

And add classification rules:
```kotlin
private fun classifyTransaction(merchant: String): String {
    return when {
        // ... existing
        text.contains(Regex("(pattern)")) -> "YOUR_CATEGORY"
    }
}
```

## Debugging

### Enable Logging
```kotlin
Log.d(TAG, "Debug message")  // Debug
Log.e(TAG, "Error", exception) // Error
```

View in logcat:
```bash
adb logcat | grep SmsReceiver
adb logcat | grep SmsParser
```

### Inspect Database

Android Studio → Device Explorer → data/data/com.example.expensetracker/databases

Or use SQLite Browser app.

### Check SMS Permissions

```kotlin
val hasPermission = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.READ_SMS
) == PackageManager.PERMISSION_GRANTED
```

## Common Issues

### "No transactions appear after SMS"
1. Check logcat for errors
2. Verify SMS sender is in isBankMessage() list
3. Test SMS parsing with hardcoded text

### "App crashes on startup"
1. Check Room annotations
2. Verify DAO methods match queries
3. Check AndroidManifest receiver registration

### "Permissions not working"
1. Ensure all 3 permissions requested in MainActivity
2. Check MinSdkVersion supports the permissions
3. Verify AndroidManifest has correct permission declarations

### "Layout looks wrong"
1. Check screen density (use different device/emulator sizes)
2. Verify Compose dependencies are correct version
3. Clear build cache: `./gradlew clean`

## Next Steps

1. **Run the app** on emulator/device
2. **Test SMS parsing** with sample messages
3. **Add initial accounts** (can be done via Room inspection)
4. **Implement remaining screens** (Accounts, Settings)
5. **Customize categories** for your banks
6. **Add notification** when transaction arrives
7. **Export feature** (CSV export of monthly data)

## Support Files

- **SMS_EXPENSE_TRACKER.md** — Full feature documentation
- **SETUP_GUIDE.md** — This file
- **Code comments** — Check for detailed explanations

Start with HomeScreen and gradually implement other screens!
