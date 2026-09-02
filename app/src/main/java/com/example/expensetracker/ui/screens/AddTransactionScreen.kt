package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.ui.AppColors
import com.example.sms_parser.util.SmsExtractors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    repository: TransactionRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val accounts by repository.getActiveAccounts().collectAsState(initial = emptyList())

    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }
    var selectedAccount by remember { mutableStateOf(if (accounts.isNotEmpty()) accounts[0].id else "") }

    val categories = listOf("Groceries", "Eating out", "Travel", "Bills", "Shopping", "Health", "Rent", "Cash")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(16.dp)
    ) {
        BackButton(onClick = onBack)

        Text(
            "Add it yourself",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(top = 18.dp)
        )

        Text(
            "For cash, and anything no bank ever texted you about.",
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Expense/Income toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToggleButton(
                text = "Spent",
                isSelected = isExpense,
                onClick = { isExpense = true },
                modifier = Modifier.weight(1f)
            )
            ToggleButton(
                text = "Received",
                isSelected = !isExpense,
                onClick = { isExpense = false },
                modifier = Modifier.weight(1f)
            )
        }

        // Amount input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.CardBackground, RoundedCornerShape(24.dp))
                .padding(18.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "₹",
                    fontSize = 34.sp,
                    color = AppColors.TextSecondary.copy(alpha = 0.35f),
                    fontFamily = FontFamily.Serif
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text("0") },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                )
            }
        }

        // Merchant input
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                "MERCHANT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 7.dp)
            )
            TextField(
                value = merchant,
                onValueChange = { merchant = it },
                placeholder = { Text("Who got the money?") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.CardBackground,
                    unfocusedContainerColor = AppColors.CardBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(999.dp)
            )
        }

        // Category selection
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                "CATEGORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 7.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    CategoryChip(
                        name = cat,
                        isSelected = category == cat,
                        onClick = { category = cat }
                    )
                }
            }
        }

        // Account selection
        Column(modifier = Modifier.padding(bottom = 22.dp)) {
            Text(
                "ACCOUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 7.dp)
            )
            accounts.forEach { account ->
                AccountSelector(
                    account = account,
                    isSelected = account.id == selectedAccount,
                    onClick = { selectedAccount = account.id }
                )
            }
        }

        // Save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Primary, RoundedCornerShape(999.dp))
                .clickable {
                    if (amount.isNotEmpty() && merchant.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val txn = Transaction(
                                id = SmsExtractors.generateTransactionId(),
                                merchant = merchant,
                                amount = if (isExpense) -amount.toDouble() else amount.toDouble(),
                                category = category,
                                accountId = selectedAccount,
                                timestamp = System.currentTimeMillis(),
                                source = "manual",
                                isIncome = !isExpense
                            )
                            repository.insertTransaction(txn)
                            onSaved()
                        }
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Save it",
                color = Color(0xFFFFF2EB),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) AppColors.Primary else AppColors.CardBackground,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color(0xFFFFF2EB) else AppColors.TextPrimary
        )
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) AppColors.TextPrimary else Color.Transparent,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp)
    ) {
        Text(
            name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color(0xFFFFF2EB) else AppColors.TextPrimary
        )
    }
}

@Composable
fun AccountSelector(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) AppColors.CardBackground else Color.Transparent,
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp, 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color(android.graphics.Color.parseColor(account.color)).copy(alpha = 0.24f))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                account.shortName.take(2),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor(account.color))
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                account.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            Text(
                account.bankCode,
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}
