package com.example.expensetracker.ui.navigation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Account
import com.example.expensetracker.ui.AppColors
import com.example.expensetracker.ui.MonthlyStats

@Composable
fun DrawerContent(
    accounts: List<Account>,
    stats: MonthlyStats,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .fillMaxHeight()
            .background(AppColors.Surface),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(26.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AppColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "P",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Surface,
                        fontFamily = FontFamily.Serif
                    )
                }

                Text(
                    "Paisa",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(top = 14.dp)
                )

                Text(
                    "₹${String.format("%.0f", stats.expense)} out in Aug",
                    fontSize = 12.5.sp,
                    color = AppColors.TextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                NavigationItem(
                    label = "Home",
                    isSelected = currentScreen == Screen.Home,
                    onClick = { onNavigate(Screen.Home) }
                )
                NavigationItem(
                    label = "Accounts",
                    isSelected = currentScreen == Screen.Accounts,
                    onClick = { onNavigate(Screen.Accounts) }
                )
                NavigationItem(
                    label = "Add a transaction",
                    isSelected = currentScreen == Screen.Add,
                    onClick = { onNavigate(Screen.Add) }
                )
                NavigationItem(
                    label = "Settings",
                    isSelected = currentScreen == Screen.Settings,
                    onClick = { onNavigate(Screen.Settings) }
                )
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth())
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            AppColors.IncomeGreen.copy(alpha = 0.18f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(15.dp, 16.dp)
                ) {
                    Text(
                        "Messages are read on your phone. Nothing leaves the device.",
                        fontSize = 12.5.sp,
                        color = AppColors.IncomeGreen.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) AppColors.CardBackground else androidx.compose.ui.graphics.Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(13.dp, 15.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .background(
                    if (isSelected) AppColors.Primary else AppColors.TextSecondary.copy(alpha = 0.25f),
                    CircleShape
                )
        )

        Text(
            label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary.copy(alpha = 0.7f)
        )
    }
}
