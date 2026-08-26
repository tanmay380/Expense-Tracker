# API Reference — SMS Expense Tracker

Quick reference for all major classes and functions.

## TransactionRepository

Central data access point. Use this in ViewModels and Screens.

```kotlin
// Get all transactions (reactive, updates automatically)
val transactions: Flow<List<Transaction>> = repository.getAllTransactions()
transactions.collect { list -> /* list updated */ }

// Or in Compose:
val transactions by repository.getAllTransactions().collectAsState(initial = emptyList())

// Search transactions
val results: Flow<List<Transaction>> = repository.searchTransactions("blinkit")

// Get transactions for specific account
val accountTxns: Flow<List<Transaction>> = repository.getTransactionsByAccount(accountId)

// Get transactions in date range
val rangeTxns: Flow<List<Transaction>> = repository.getTransactionsByDateRange(startTime, endTime)

// Single transaction lookup
val txn: Transaction? = repository.getTransactionById(id)

// Add transaction
repository.insertTransaction(transaction)

// Update transaction
repository.updateTransaction(transaction.copy(category = "Groceries"))

// Delete transaction
repository.deleteTransaction(transaction)

// Get accounts
val accounts: Flow<List<Account>> = repository.getActiveAccounts()
val allAccounts: Flow<List<Account>> = repository.getAllAccounts()

// Account lookup
val account: Account? = repository.getAccountById(accountId)
val accountByBank: Account? = repository.getAccountByBankCode("ICICI")

// Add/update account
repository.insertAccount(account)
repository.updateAccount(account)
```

## SmsParser

Extracts transaction details from SMS messages.

```kotlin
// Parse single SMS
val parsed = SmsParser.parseTransaction(smsBody, smsSender)
if (parsed != null) {
    val amount: Double = parsed.amount
    val merchant: String = parsed.merchant
    val accountNumber: String = parsed.accountNumber
    val bankCode: String = parsed.bankCode
    val isIncome: Boolean = parsed.isIncome
}

// Check if SMS is from a bank
val isBank: Boolean = SmsParser.isBankMessage("AD-SLICE") // true
val isNotBank: Boolean = SmsParser.isBankMessage("1234567890") // false

// Generate transaction ID
val id: String = SmsParser.generateTransactionId() // UUID
```

## MainViewModel

State management and filtering logic.

```kotlin
val viewModel = MainViewModel(repository)

// Get reactive states
val selectedMonth: StateFlow<YearMonth> = viewModel.selectedMonth
val searchQuery: StateFlow<String> = viewModel.searchQuery
val selectedAccountId: StateFlow<String?> = viewModel.selectedAccountId

// Get reactive data flows
val transactions: Flow<List<Transaction>> = viewModel.transactions
val accounts: Flow<List<Account>> = viewModel.accounts

// Month navigation
viewModel.previousMonth() // Go to previous month
viewModel.nextMonth()     // Go to next month

// Search
viewModel.updateSearchQuery("blinkit") // Updates searchQuery state

// Account filter
viewModel.selectAccount(accountId) // Filter by account
viewModel.selectAccount(null)       // Clear account filter

// Get filtered and sorted transactions
val filtered: List<Transaction> = viewModel.getFilteredTransactions(
    transactions = allTransactions,
    accounts = allAccounts
)

// Calculate monthly totals
val stats: MonthlyStats = viewModel.calculateMonthlyStats(transactions)
// stats.income, stats.expense, stats.net
```

## UI Screens

### HomeScreen

Main transaction list with stats and navigation.

```kotlin
HomeScreen(
    viewModel = viewModel,
    onTransactionClick = { transaction ->
        // Handle click - navigate to detail
    },
    onAddClick = {
        // Handle add button - navigate to add screen
    }
)
```

Internal components:
```kotlin
MonthHeader(
    month = YearMonth.now(),
    onPreviousMonth = { },
    onNextMonth = { }
)

StatsCard(stats = MonthlyStats(income = 5000.0, expense = 2000.0))

TransactionRow(
    transaction = transaction,
    onClick = { }
)
```

### TransactionDetailScreen

Full transaction details with SMS content.

```kotlin
TransactionDetailScreen(
    transaction = transaction,
    account = account,
    onBack = { /* navigate back */ }
)
```

### AddTransactionScreen

Manual transaction entry form.

```kotlin
AddTransactionScreen(
    repository = repository,
    onBack = { /* navigate back */ },
    onSaved = { /* navigate to home */ }
)
```

## UiUtils

Formatting and styling utilities.

```kotlin
// Format amounts
val formatted: String = UiUtils.formatAmount(842.5)        // "+₹842.50"
val compact: String = UiUtils.formatAmountCompact(50000.0) // "+₹50,000"

// Format dates and times
val dateStr: String = UiUtils.formatDate(timestamp) // "Today", "Yesterday", "Mon, 23 Aug"
val timeStr: String = UiUtils.formatTime(timestamp) // "4:12 pm"

// Get colors
val color: Color = UiUtils.getColorForCategory("Groceries") // Color object
val bgColor: Color = UiUtils.getBackgroundColorForCategory("Eating out")

// Pre-defined colors
AppColors.Primary      // #C67139
AppColors.Surface      // #F5EAD8
AppColors.Background   // #EFE3CD
AppColors.TextPrimary  // #201E1D
AppColors.TextSecondary// #645C50
AppColors.IncomeGreen  // #7A8A5E
AppColors.ExpenseRed   // #8C491A
```

## Data Models

### Transaction

```kotlin
data class Transaction(
    val id: String = "",                    // Primary key (UUID)
    val merchant: String = "",              // Store/company name
    val amount: Double = 0.0,               // Negative = expense, positive = income
    val category: String = "Shopping",      // Auto-classified or manual
    val accountId: String = "",             // Link to Account
    val timestamp: Long = System.currentTimeMillis(), // When it happened
    val source: String = "sms",             // "sms" or "manual"
    val smsSender: String? = null,          // SMS sender ID
    val smsContent: String? = null,         // Full SMS text
    val isIncome: Boolean = false,          // Income vs expense
)

// Create transaction
val txn = Transaction(
    id = SmsParser.generateTransactionId(),
    merchant = "BLINKIT",
    amount = -842.0,
    category = "Groceries",
    accountId = "icici-1",
    timestamp = System.currentTimeMillis(),
    source = "sms",
    smsSender = "AD-SLICE",
    smsContent = "INR 842.00 spent...",
    isIncome = false
)
```

### Account

```kotlin
data class Account(
    val id: String = "",                    // Primary key (UUID)
    val name: String = "",                  // "ICICI Bank ••3717"
    val shortName: String = "",             // "ICICI ••3717" (for UI)
    val bankCode: String = "",              // "ICICI", "HDFC", etc.
    val accountNumber: String = "",         // Last 4 digits "3717"
    val initialBalance: Double = 0.0,       // Starting balance
    val color: String = "#C67139",          // Hex color for UI
    val isActive: Boolean = true,           // Show in lists
)

// Create account
val account = Account(
    id = UUID.randomUUID().toString(),
    name = "ICICI Bank ••3717",
    shortName = "ICICI ••3717",
    bankCode = "ICICI",
    accountNumber = "3717",
    initialBalance = 50000.0,
    color = "#C67139",
    isActive = true
)
```

### MonthlyStats

```kotlin
data class MonthlyStats(
    val income: Double = 0.0,   // Total income in month
    val expense: Double = 0.0,  // Total expenses in month (absolute value)
    val net: Double = 0.0       // income - expense
)
```

## SmsReceiver

Automatic SMS interception and processing.

```kotlin
// BroadcastReceiver that runs on SMS arrival
// Automatically registered in AndroidManifest.xml

// You don't call this directly - Android does:
// SMS arrives → SmsReceiver.onReceive() called automatically
// → Parses SMS → Inserts into database → Done!

// Inside onReceive():
// 1. Check SMS is from bank
// 2. Parse transaction details
// 3. Get/create account
// 4. Classify transaction
// 5. Insert into Room DB
// 6. Flow emits → UI updates
```

## Room Database Operations

Direct DAO usage (usually done through Repository):

```kotlin
// Get DAO
val transactionDao: TransactionDao = db.transactionDao()
val accountDao: AccountDao = db.accountDao()

// Insert
transactionDao.insert(transaction)
accountDao.insert(account)

// Update
transactionDao.update(transaction)
accountDao.update(account)

// Delete
transactionDao.delete(transaction)

// Query single
val txn: Transaction? = transactionDao.getTransactionById(id)
val acc: Account? = accountDao.getAccountById(id)

// Query flow
val allTxns: Flow<List<Transaction>> = transactionDao.getAllTransactions()
val activeAccounts: Flow<List<Account>> = accountDao.getActiveAccounts()

// Search
val results: Flow<List<Transaction>> = transactionDao.searchTransactions("blinkit")
```

## Permissions

```kotlin
// Check permission
val hasSmsRead: Boolean = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.READ_SMS
) == PackageManager.PERMISSION_GRANTED

// Request permission (in Activity)
registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) { /* Permission granted */ }
}.launch(Manifest.permission.READ_SMS)
```

## Compose State Management

```kotlin
// In Composable function:

// Collect Flow as State
val transactions by viewModel.transactions.collectAsState(initial = emptyList())

// Collect StateFlow
val selectedMonth by viewModel.selectedMonth.collectAsState()

// These automatically recompose when values change
Text("${transactions.size} transactions")

// Update state from user action
Button(onClick = { viewModel.updateSearchQuery("blinkit") })

// Use in LazyColumn
LazyColumn {
    items(transactions) { txn ->
        TransactionRow(transaction = txn)
    }
}
```

## Common Patterns

### Observe all transactions and accounts
```kotlin
val transactions by viewModel.transactions.collectAsState(initial = emptyList())
val accounts by viewModel.accounts.collectAsState(initial = emptyList())

val filtered = viewModel.getFilteredTransactions(transactions, accounts)
```

### Navigate to transaction detail
```kotlin
HomeScreen(
    onTransactionClick = { txn ->
        // Save selection
        selectedTransaction = txn
        // Change screen
        currentScreen = Screen.Detail
    }
)
```

### Add new transaction
```kotlin
repository.insertTransaction(Transaction(
    id = SmsParser.generateTransactionId(),
    merchant = "BLINKIT",
    amount = -500.0,
    category = "Groceries",
    accountId = accountId,
    timestamp = System.currentTimeMillis(),
    source = "manual"
))
// Flow emits automatically → UI updates
```

### Filter by month
```kotlin
val selectedMonth: YearMonth = YearMonth.now()
val monthStart: Long = selectedMonth.atDay(1)
    .atStartOfDay(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()
val monthEnd: Long = selectedMonth.atEndOfMonth()
    .atTime(23, 59, 59)
    .atZone(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

val monthTransactions: Flow<List<Transaction>> = 
    repository.getTransactionsByDateRange(monthStart, monthEnd)
```

---

**For detailed implementation examples, see:**
- SMS_EXPENSE_TRACKER.md
- SETUP_GUIDE.md
- Source code files with inline comments
