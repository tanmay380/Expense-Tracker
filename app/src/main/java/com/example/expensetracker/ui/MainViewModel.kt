package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.data.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    private val TAG = "MainViewModel"

    init {
        Log.d(TAG, "🔧 ViewModel initialized")
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    // Convert Room Flow to StateFlow for hot emission
    val transactions: Flow<List<Transaction>> = repository.getAllTransactions()
        .onEach { list ->
            Log.d(TAG, "🔄 Room Flow emitted: ${list.size} transactions")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        ).also {
            Log.d(TAG, "📡 Transactions StateFlow created")
        }

    val accounts: Flow<List<Account>> = repository.getActiveAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        loadInitialData()
    }



    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Success(
                    transactions = emptyList(),
                    accounts = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectAccount(accountId: String?) {
        _selectedAccountId.value = accountId
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun getFilteredTransactions(
        transactions: List<Transaction>,
        accounts: List<Account>
    ): List<Transaction> {
        val month = _selectedMonth.value
        val query = _searchQuery.value.trim().lowercase()
        val accountId = _selectedAccountId.value

        return transactions.filter { txn ->
            val txnDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(txn.timestamp),
                ZoneId.systemDefault()
            )
            val txnMonth = YearMonth.of(txnDate.year, txnDate.monthValue)

            val matchesMonth = txnMonth == month
            val matchesAccount = accountId == null || txn.accountId == accountId
            val matchesSearch = if (query.isEmpty()) {
                true
            } else {
                txn.merchant.lowercase().contains(query) ||
                    txn.category.lowercase().contains(query)
            }

            matchesMonth && matchesAccount && matchesSearch
        }
    }

    fun calculateMonthlyStats(transactions: List<Transaction>): MonthlyStats {
        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { kotlin.math.abs(it.amount) }

        return MonthlyStats(
            income = income,
            expense = expense,
            net = income - expense
        )
    }

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val transactions: List<Transaction>,
            val accounts: List<Account>
        ) : UiState()
        data class Error(val message: String) : UiState()
    }
}

data class MonthlyStats(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val net: Double = 0.0
)
