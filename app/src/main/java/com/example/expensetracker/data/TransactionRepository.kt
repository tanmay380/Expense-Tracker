package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccount(accountId)

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startTime, endTime)

    fun searchTransactions(query: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(query)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun getTransactionById(id: String): Transaction? =
        transactionDao.getTransactionById(id)

    fun getActiveAccounts(): Flow<List<Account>> = accountDao.getActiveAccounts()

    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun insertAccount(account: Account) {
        accountDao.insert(account)
    }

    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }

    suspend fun getAccountById(id: String): Account? =
        accountDao.getAccountById(id)

    suspend fun getAccountByBankCode(bankCode: String): Account? =
        accountDao.getAccountByBankCode(bankCode)
}
