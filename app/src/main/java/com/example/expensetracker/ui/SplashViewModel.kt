package com.example.expensetracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.util.SmsReader
import com.example.expensetracker.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Initial)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkIfShouldShowSplash()
    }

    private fun checkIfShouldShowSplash() {
        viewModelScope.launch {
            val hasSeenSplash = preferencesManager.hasSeenImportSplash()
            if (hasSeenSplash) {
                _uiState.value = SplashUiState.Completed(processedCount = 0)
                delay(500)
            }
        }
    }

    fun startImportProcess() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Processing
            preferencesManager.setImportSplashSeen(true)

            try {
                val processedCount = SmsReader.scanAllMessages(context, repository)
                delay(1000)
                _uiState.value = SplashUiState.Completed(processedCount = processedCount)
                delay(2000)
            } catch (e: Exception) {
                _uiState.value = SplashUiState.Completed(processedCount = 0)
                delay(2000)
            }
        }
    }

    fun skipImportProcess() {
        viewModelScope.launch {
            preferencesManager.setImportSplashSeen(true)
            _uiState.value = SplashUiState.Completed(processedCount = 0)
            delay(500)
        }
    }

    sealed class SplashUiState {
        object Initial : SplashUiState()
        object Processing : SplashUiState()
        data class Completed(val processedCount: Int) : SplashUiState()
    }
}
