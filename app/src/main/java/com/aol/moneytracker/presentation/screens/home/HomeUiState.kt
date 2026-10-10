package com.aol.moneytracker.presentation.screens.home

import androidx.annotation.StringRes
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.domain.model.Transaction

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val summary: FinancialSummary = FinancialSummary(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

enum class TransactionFilter(
    val label: String,
    @StringRes val labelRes: Int
) {
    ALL("Semua", R.string.filter_all),
    EXPENSE("Pengeluaran", R.string.type_expense),
    INCOME("Pemasukan", R.string.type_income)
}
