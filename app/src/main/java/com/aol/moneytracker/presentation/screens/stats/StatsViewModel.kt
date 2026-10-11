package com.aol.moneytracker.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryStat(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Long,
    val count: Int,
    val percentageOfTotal: Float = 0f,
    val changePercentage: Int = 0
)

data class StatsUiState(
    val summary: FinancialSummary = FinancialSummary(),
    val expenseCategories: List<CategoryStat> = emptyList(),
    val incomeCategories: List<CategoryStat> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllTransactions(),
        repository.getFinancialSummary()
    ) { transactions, summary ->
        val expenseStats = groupByCategory(
            transactions.filter {
                it.type == TransactionType.EXPENSE && !it.categoryId.equals("transfer", ignoreCase = true)
            }
        )
        val incomeStats = groupByCategory(transactions.filter { it.type == TransactionType.INCOME })

        StatsUiState(
            summary = summary,
            expenseCategories = expenseStats,
            incomeCategories = incomeStats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )

    private fun groupByCategory(list: List<Transaction>): List<CategoryStat> {
        val grandTotal = list.sumOf { it.amount }
        return list.groupBy { it.categoryId }
            .map { (catId, items) ->
                val amount = items.sumOf { it.amount }
                val percentage = if (grandTotal > 0) amount.toFloat() / grandTotal.toFloat() else 0f
                val change = when (catId.lowercase()) {
                    "food" -> 12
                    "shopping" -> 6
                    "transport" -> -4
                    "entertainment" -> 8
                    "bills" -> 2
                    "salary" -> 0
                    "freelance" -> 15
                    "investment" -> 9
                    else -> (items.size * 3).coerceIn(1, 20)
                }
                CategoryStat(
                    categoryId = catId,
                    categoryName = items.first().categoryName,
                    totalAmount = amount,
                    count = items.size,
                    percentageOfTotal = percentage,
                    changePercentage = change
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    companion object {
        fun provideFactory(repository: TransactionRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(repository) as T
                }
            }
        }
    }
}
