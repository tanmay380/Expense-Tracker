package com.example.expensetracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.ui.AppColors
import com.example.expensetracker.ui.SplashViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor by animateColorAsState(
        targetValue = AppColors.Background,
        animationSpec = tween(500)
    )

    LaunchedEffect(uiState) {
        if (uiState is SplashViewModel.SplashUiState.Completed) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is SplashViewModel.SplashUiState.Initial -> {
                SplashInitialContent(
                    onImportClick = { viewModel.startImportProcess() },
                    onSkipClick = { viewModel.skipImportProcess() }
                )
            }

            is SplashViewModel.SplashUiState.Processing -> {
                SplashProcessingContent()
            }

            is SplashViewModel.SplashUiState.Completed -> {
                SplashCompletedContent(
                    processedCount = (uiState as SplashViewModel.SplashUiState.Completed).processedCount
                )
            }
        }
    }
}

@Composable
private fun SplashInitialContent(
    onImportClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = AppColors.Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💰",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Paisa Expense Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Smart expense tracking from your SMS messages",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppColors.Primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "📱 Import Previous Transactions?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Text
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We can scan your SMS messages to automatically detect bank transactions and accounts from your previous messages.",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✓ Only bank messages are processed\n✓ No personal data is stored\n✓ You can do this later",
                    fontSize = 11.sp,
                    color = AppColors.TextSecondary.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Yes, Import Transactions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSkipClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Skip for Now",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Primary
            )
        }
    }
}

@Composable
private fun SplashProcessingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .height(60.dp)
                .padding(8.dp),
            color = AppColors.Primary,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Scanning SMS Messages",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Detecting bank transactions and accounts...",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This may take a few moments",
            fontSize = 12.sp,
            color = AppColors.TextSecondary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SplashCompletedContent(processedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 48.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Import Complete!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (processedCount > 0) {
                "Successfully imported $processedCount transactions"
            } else {
                "No new transactions found"
            },
            fontSize = 16.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Opening your dashboard...",
            fontSize = 12.sp,
            color = AppColors.TextSecondary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
