package com.aol.moneytracker.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllTransactions(),
        repository.getFinancialSummary(),
        _filter,
        _searchQuery,
        com.aol.moneytracker.util.LanguageManager.currentLanguage
    ) { allTransactions, summary, filter, query, _ ->
        val filtered = allTransactions.filter { item ->
            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> item.type == TransactionType.EXPENSE
                TransactionFilter.INCOME -> item.type == TransactionType.INCOME
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                item.title.contains(query, ignoreCase = true) ||
                        item.categoryName.contains(query, ignoreCase = true) ||
                        item.note.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }

        val grouped = filtered.groupBy { com.aol.moneytracker.util.DateFormatter.formatDateGroup(it.dateMillis) }

        HomeUiState(
            transactions = filtered,
            groupedTransactions = grouped,
            summary = summary,
            selectedFilter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onFilterChange(filter: TransactionFilter) {
        _filter.value = filter
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    companion object {
        fun provideFactory(repository: TransactionRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}
