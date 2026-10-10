package com.aol.moneytracker.domain.repository

import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun getFinancialSummary(): Flow<FinancialSummary>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
}
