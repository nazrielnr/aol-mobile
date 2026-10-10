package com.aol.moneytracker.domain.model

import androidx.annotation.StringRes
import com.aol.moneytracker.R

enum class TransactionType(
    val displayName: String,
    @StringRes val labelRes: Int
) {
    EXPENSE("Pengeluaran", R.string.type_expense),
    INCOME("Pemasukan", R.string.type_income)
}
