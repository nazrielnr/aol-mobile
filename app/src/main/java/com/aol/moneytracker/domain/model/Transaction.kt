package com.aol.moneytracker.domain.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val dateMillis: Long,
    val note: String = ""
)
