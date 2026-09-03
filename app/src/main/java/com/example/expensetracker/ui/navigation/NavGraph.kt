package com.example.expensetracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.ui.AppColors
import com.example.expensetracker.ui.MainViewModel
import com.example.expensetracker.ui.MonthlyStats
import com.example.expensetracker.ui.screens.AddTransactionScreen
import com.example.expensetracker.ui.screens.HomeScreen
import com.example.expensetracker.ui.screens.TransactionDetailScreen
import com.example.expensetracker.ui.screens.SplashScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Detail : Screen("detail/{transactionId}") {
        fun createRoute(transactionId: String) = "detail/$transactionId"
    }
    object Add : Screen("add")
    object Accounts : Screen("accounts")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: TransactionRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.Home as Screen) }

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())

    val stats = viewModel.calculateMonthlyStats(transactions)

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                accounts = accounts,
                stats = stats,
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    currentScreen = screen
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        },
        drawerState = drawerState,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Background
        ) {
            NavGraph_Internal(
                navController = navController,
                repository = repository,
                viewModel = viewModel,
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onScreenChange = { screen ->
                    currentScreen = screen
                }
            )
        }
    }
}

@Composable
private fun NavGraph_Internal(
    navController: NavHostController,
    repository: TransactionRepository,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onScreenChange: (Screen) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            onScreenChange(Screen.Home)
            HomeScreen(
                viewModel = viewModel,
                onTransactionClick = { txn ->
                    navController.navigate(Screen.Detail.createRoute(txn.id))
                },
                onAddClick = {
                    navController.navigate(Screen.Add.route)
                },
                onMenuClick = onMenuClick
            )
        }

        composable(
            Screen.Detail.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: return@composable
            var transaction by remember { mutableStateOf<com.example.expensetracker.data.Transaction?>(null) }
            var account by remember { mutableStateOf<com.example.expensetracker.data.Account?>(null) }

            androidx.compose.runtime.LaunchedEffect(transactionId) {
                transaction = repository.getTransactionById(transactionId)
            }

            androidx.compose.runtime.LaunchedEffect(transaction?.accountId) {
                if (transaction != null) {
                    account = repository.getAccountById(transaction!!.accountId)
                }
            }

            if (transaction != null && account != null) {
                TransactionDetailScreen(
                    transaction = transaction!!,
                    account = account!!,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Add.route) {
            onScreenChange(Screen.Add)
            AddTransactionScreen(
                repository = repository,
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Accounts.route) {
            onScreenChange(Screen.Accounts)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text("Accounts Screen - Coming soon")
            }
        }

        composable(Screen.Settings.route) {
            onScreenChange(Screen.Settings)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text("Settings Screen - Coming soon")
            }
        }
    }
}
