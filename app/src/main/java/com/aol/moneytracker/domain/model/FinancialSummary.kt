package com.aol.moneytracker.domain.model

data class FinancialSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L
) {
    val balance: Long get() = totalIncome - totalExpense
}
