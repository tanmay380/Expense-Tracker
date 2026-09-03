package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.AppColors
import com.example.expensetracker.ui.MainViewModel
import com.example.expensetracker.ui.UiUtils

@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    account: Account,
    viewModel: MainViewModel? = null,
    onBack: () -> Unit
) {
    var showCategoryPicker by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableStateOf(transaction.category) }
    val categories by (viewModel?.categories?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(16.dp)
    ) {
        // Back button
        BackButton(onClick = onBack)

        // Transaction avatar and amount
        Box(
            modifier = Modifier
                .size(66.dp)
                .background(
                    UiUtils.getBackgroundColorForCategory(transaction.category),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                transaction.merchant.take(2).uppercase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = UiUtils.getColorForCategory(transaction.category),
                fontFamily = FontFamily.Serif
            )
        }

        Text(
            UiUtils.formatAmount(transaction.amount),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isIncome) AppColors.IncomeGreen else AppColors.TextPrimary,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            transaction.merchant,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            "${UiUtils.formatDate(transaction.timestamp)} · ${UiUtils.formatTime(transaction.timestamp)}",
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.CardBackground, RoundedCornerShape(24.dp))
                .padding(18.dp)
                .padding(top = 20.dp)
        ) {
            DetailRow("Category", currentCategory)
            DetailDivider()
            DetailRow("Account", account.name, isClickable = true)
            DetailDivider()
            DetailRow("Added by", if (transaction.source == "sms") "Read from SMS" else "Added by you")
        }

        // SMS details if available
        if (transaction.source == "sms" && transaction.smsContent != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    "THE MESSAGE IT CAME FROM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF7A8A5E).copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                transaction.smsSender ?: "Unknown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF56633F)
                            )
                            Text(
                                "${UiUtils.formatDate(transaction.timestamp)}, ${UiUtils.formatTime(transaction.timestamp)}",
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                        Text(
                            transaction.smsContent!!,
                            fontSize = 13.sp,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(top = 12.dp),
                            softWrap = true
                        )
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButton(
                text = "Change category",
                onClick = { showCategoryPicker = true },
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Split it",
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showCategoryPicker && viewModel != null) {
        CategoryPickerScreen(
            categories = categories,
            currentCategory = currentCategory,
            onCategorySelected = { newCategory ->
                currentCategory = newCategory
                viewModel.updateTransactionCategory(transaction.id, newCategory)
            },
            onAddCategory = { customCategory ->
                viewModel.addCustomCategory(customCategory)
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, isClickable: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { } else Modifier)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = AppColors.TextSecondary
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary
        )
    }
}

@Composable
fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Divider)
            .padding(vertical = 0.5.dp)
    )
}

@Composable
fun BackButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(AppColors.CardBackground, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("‹", fontSize = 13.sp, color = AppColors.TextSecondary)
        Text("Home", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    Box(
        modifier = modifier
            .background(
                if (isPrimary) AppColors.Primary else Color.Transparent,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isPrimary) Color(0xFFFFF2EB) else AppColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TransactionDetailScreenPreview() {
    TransactionDetailScreen(
        transaction = Transaction(
            id = "1",
            merchant = "Amazon",
            amount = 1299.0,
            category = "Shopping",
            accountId = "1",
            timestamp = System.currentTimeMillis(),
            source = "sms",
            smsSender = "9315926219",
            smsContent = "Rs. 1299.00 spent on Amazon via credit card ending with 1234 on 25-Aug-2026 15:05:54. Available balance: Rs. 10000.00",
            isIncome = false
        ),
        account = Account(
            id = "1",
            name = "Credit Card",
            shortName = "CC",
            bankCode = "HDFC",
            accountNumber = "1234",
            initialBalance = 10000.0,
            color = "#C67139",
            isActive = true
        ),
        onBack = {}
    )
}
