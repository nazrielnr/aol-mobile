package com.aol.moneytracker.domain.model

import androidx.annotation.StringRes
import com.aol.moneytracker.R

data class Category(
    val id: String,
    val name: String,
    @StringRes val nameRes: Int,
    val type: TransactionType,
    val iconKey: String
) {
    companion object {
        val EXPENSE_CATEGORIES = listOf(
            Category("food", "Makanan & Minuman", R.string.category_food, TransactionType.EXPENSE, "restaurant"),
            Category("transport", "Transportasi", R.string.category_transport, TransactionType.EXPENSE, "directions_car"),
            Category("shopping", "Belanja", R.string.category_shopping, TransactionType.EXPENSE, "shopping_bag"),
            Category("bills", "Tagihan & Utilitas", R.string.category_bills, TransactionType.EXPENSE, "receipt_long"),
            Category("entertainment", "Hiburan", R.string.category_entertainment, TransactionType.EXPENSE, "sports_esports"),
            Category("health", "Kesehatan", R.string.category_health, TransactionType.EXPENSE, "medical_services"),
            Category("education", "Pendidikan", R.string.category_education, TransactionType.EXPENSE, "school"),
            Category("other_expense", "Lainnya", R.string.category_other, TransactionType.EXPENSE, "more_horiz")
        )

        val INCOME_CATEGORIES = listOf(
            Category("salary", "Gaji Pokok", R.string.category_salary, TransactionType.INCOME, "account_balance_wallet"),
            Category("bonus", "Bonus & THR", R.string.category_bonus, TransactionType.INCOME, "card_giftcard"),
            Category("investment", "Investasi", R.string.category_investment, TransactionType.INCOME, "trending_up"),
            Category("freelance", "Proyek Lepas", R.string.category_freelance, TransactionType.INCOME, "laptop_mac"),
            Category("business", "Usaha & Bisnis", R.string.category_business, TransactionType.INCOME, "storefront"),
            Category("other_income", "Lainnya", R.string.category_other, TransactionType.INCOME, "savings")
        )

        fun findCategory(id: String, type: TransactionType): Category {
            val list = if (type == TransactionType.INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES
            return list.firstOrNull { it.id == id } ?: list.last()
        }

        @StringRes
        fun getCategoryNameRes(id: String): Int {
            return (EXPENSE_CATEGORIES + INCOME_CATEGORIES)
                .firstOrNull { it.id.equals(id, ignoreCase = true) }
                ?.nameRes ?: R.string.category_other
        }
    }
}
