package com.example.expensetracker.ui.screens

import android.graphics.drawable.shapes.OvalShape
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.AppColors
import com.example.expensetracker.ui.MainViewModel
import com.example.expensetracker.ui.MonthlyStats
import com.example.expensetracker.ui.UiUtils
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val TAG = "HomeScreen"

    Log.d(TAG, "🏠 HomeScreen composing...")

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    Log.d(TAG, "✅ Subscribed to transactions Flow. Initial size: ${transactions.size}")

    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        Log.d(TAG, "🚀 HomeScreen LaunchedEffect started")
    }

    LaunchedEffect(transactions.size) {
        Log.d(TAG, "📊 TRANSACTIONS CHANGED! New count: ${transactions.size}")
    }

    val filteredTransactions = viewModel.getFilteredTransactions(transactions, accounts)
    val stats = viewModel.calculateMonthlyStats(filteredTransactions)

    LaunchedEffect(transactions) {
        Log.d("tanmay", "HomeScreen: ${transactions.size}")
    }

    HomeScreenContent(
        accounts = accounts,
        selectedMonth = selectedMonth,
        searchQuery = searchQuery,
        filteredTransactions = filteredTransactions,
        stats = stats,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onMenuClick = onMenuClick,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onTransactionClick = onTransactionClick
    )
}

@Composable
fun HomeScreenContent(
    accounts: List<Account>,
    selectedMonth: YearMonth,
    searchQuery: String,
    filteredTransactions: List<Transaction>,
    stats: MonthlyStats,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMenuClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
            )
    ) {
        // Header with month navigation
        MonthHeader(
            month = selectedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onMenuClick = onMenuClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            }

            // Stats card
            item {
                StatsCard(stats = stats)
            }

            // Accounts section
            item {
                if (accounts.isNotEmpty()) {
                    AccountsSection(accounts = accounts)
                }
            }

            // Transactions header
            if (filteredTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Transactions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            "${filteredTransactions.size} of ${filteredTransactions.size}",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            // Transactions grouped by date
            val groupedByDate = filteredTransactions.groupBy { txn ->
                UiUtils.formatDate(txn.timestamp)
            }.toSortedMap(compareBy<String> {
                when (it) {
                    "Today" -> 0
                    "Yesterday" -> 1
                    else -> 2
                }
            })

            groupedByDate.forEach { (date, txns) ->
                item {
                    DateHeader(
                        date = date,
                        total = txns.filter { !it.isIncome }.sumOf { kotlin.math.abs(it.amount) }
                    )
                }

                items(txns) { txn ->
                    TransactionRow(
                        transaction = txn,
                        onClick = { onTransactionClick(txn) }
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions yet",
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val currentMonth = YearMonth.now()
    val isNextMonthInFuture = month.plusMonths(1) > currentMonth
    val monthDisplayName = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuButton(onClick = onMenuClick)

            Text(
                "Paisa",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(
                "HEY ADITYA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    monthDisplayName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthNavButton(text = "‹", onClick = onPreviousMonth, enabled = true)
                    MonthNavButton(text = "›", onClick = onNextMonth, enabled = !isNextMonthInFuture)
                }
            }
        }
    }
}

@Composable
fun MenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(17.dp)
                    .height(2.5.dp)
                    .background(AppColors.TextPrimary, shape = RoundedCornerShape(1.dp))
            )
            Box(
                modifier = Modifier
                    .width(17.dp)
                    .height(2.5.dp)
                    .background(AppColors.TextPrimary, shape = RoundedCornerShape(1.dp))
            )
            Box(
                modifier = Modifier
                    .width(11.dp)
                    .height(2.5.dp)
                    .background(AppColors.TextPrimary, shape = RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun MonthNavButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                if (enabled) AppColors.CardBackground else AppColors.CardBackground.copy(alpha = 0.5f),
                RoundedCornerShape(999.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 18.sp,
            color = if (enabled) AppColors.TextSecondary else AppColors.TextSecondary.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StatsCard(stats: MonthlyStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Money Out
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.ExpenseRed, CircleShape)
                    )
                    Text(
                        "MONEY OUT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                }
                Text(
                    UiUtils.formatAmountCompact(stats.expense),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ExpenseRed,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.01f)
                    .background(AppColors.Divider)
            )

            // Money In
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.IncomeGreen, CircleShape)
                    )
                    Text(
                        "MONEY IN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                }
                Text(
                    UiUtils.formatAmountCompact(stats.income),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.IncomeGreen,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search merchant, category, account",
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 2.dp)
            .background(AppColors.CardBackground, RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = AppColors.CardBackground,
            focusedContainerColor = AppColors.CardBackground,
            unfocusedTextColor = AppColors.TextPrimary,
            focusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.Primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}

@Composable
fun AccountsSection(accounts: List<Account>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AccountsRow("All Accounts")
        }
        items(accounts) { account ->
            AccountsRow(account.name)
        }
    }
}

@Composable
private fun AccountsRow(account: String) {
    Box(
        modifier = Modifier
            .border(
                border = BorderStroke(
                    1.dp,
                    color = Color.Black.copy(alpha = 0.2f)
                ), RoundedCornerShape(60.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            account,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun DateHeader(date: String, total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            date,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSecondary
        )
        if (total > 0) {
            Text(
                "₹${String.format("%.0f", total)} out",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    UiUtils.getBackgroundColorForCategory(transaction.category),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                transaction.merchant.take(2).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = UiUtils.getColorForCategory(transaction.category),
                fontFamily = FontFamily.Serif
            )
        }

        // Details
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                transaction.merchant,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 1
            )
            Text(
                transaction.category,
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                maxLines = 1
            )
        }

        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                UiUtils.formatAmount(transaction.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) AppColors.IncomeGreen else AppColors.TextPrimary
            )
            Text(
                if (transaction.source == "sms") "SMS" else "Manual",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.source == "sms") AppColors.IncomeGreen else AppColors.TextSecondary
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        accounts = listOf(
            Account(id = "1", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234"),
            Account(id = "2", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234"),
            Account(id = "2", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234"),
            Account(id = "2", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234"),
            Account(id = "2", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234"),
            Account(id = "2", name = "Test 1234", shortName = "Test", bankCode = "TST", accountNumber = "1234")
        ),
        selectedMonth = YearMonth.now(),
        searchQuery = "",
        filteredTransactions = listOf(
            Transaction(
                id = "1",
                merchant = "Amazon",
                amount = 1200.0,
                category = "Shopping",
                accountId = "1",
                timestamp = System.currentTimeMillis(),
                source = "manual",
                isIncome = false
            ),
            Transaction(
                id = "2",
                merchant = "Salary",
                amount = 50000.0,
                category = "Income",
                accountId = "1",
                timestamp = System.currentTimeMillis() - 86400000,
                source = "sms",
                isIncome = true
            )
        ),
        stats = MonthlyStats(expense = 1200.0, income = 50000.0),
        onPreviousMonth = {},
        onNextMonth = {},
        onMenuClick = {},
        onSearchQueryChange = {},
        onTransactionClick = {}
    )
}
