package com.aol.moneytracker.data.repository

import com.aol.moneytracker.data.local.dao.TransactionDao
import com.aol.moneytracker.data.local.entity.TransactionEntity
import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return dao.getTransactionsByType(type.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(dao.getTotalIncome(), dao.getTotalExpense()) { income, expense ->
            FinancialSummary(
                totalIncome = income,
                totalExpense = expense
            )
        }
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return dao.insert(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.update(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun deleteTransaction(id: Long) {
        dao.deleteById(id)
    }
}
