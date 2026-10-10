package com.aol.moneytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val categoryId: String,
    val categoryName: String,
    val dateMillis: Long,
    val note: String
) {
    fun toDomain(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
            categoryId = categoryId,
            categoryName = categoryName,
            dateMillis = dateMillis,
            note = note
        )
    }

    companion object {
        fun fromDomain(domain: Transaction): TransactionEntity {
            return TransactionEntity(
                id = domain.id,
                title = domain.title,
                amount = domain.amount,
                type = domain.type.name,
                categoryId = domain.categoryId,
                categoryName = domain.categoryName,
                dateMillis = domain.dateMillis,
                note = domain.note
            )
        }
    }
}
