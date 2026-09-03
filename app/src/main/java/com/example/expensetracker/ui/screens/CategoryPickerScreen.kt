package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Category
import com.example.expensetracker.ui.AppColors

@Composable
fun CategoryPickerScreen(
    categories: List<Category>,
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(AppColors.CardBackground, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Change Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryOption(
                            category = category.name,
                            isSelected = currentCategory == category.name,
                            onClick = {
                                onCategorySelected(category.name)
                                onDismiss()
                            }
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AppColors.Divider)
                                .padding(vertical = 8.dp)
                        )
                    }

                    item {
                        AddCategoryOption(
                            onClick = { showAddCategoryDialog = true }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Cancel",
                            color = AppColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    "Add Custom Category",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    TextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("Enter category name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.CardBackground, RoundedCornerShape(8.dp)),
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
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .background(AppColors.Primary, RoundedCornerShape(8.dp))
                        .clickable(enabled = newCategoryName.isNotBlank()) {
                            onAddCategory(newCategoryName)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Add",
                        color = Color(0xFFFFF2EB),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable {
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cancel",
                        color = AppColors.TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
fun CategoryOption(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) AppColors.Primary.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                category,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) AppColors.Primary else AppColors.TextPrimary
            )
            if (isSelected) {
                Text(
                    "✓",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
        }
    }
}

@Composable
fun AddCategoryOption(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "+ Add Custom Category",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Primary
        )
    }
}
