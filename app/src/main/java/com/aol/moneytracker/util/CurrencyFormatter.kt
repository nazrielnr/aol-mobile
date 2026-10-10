package com.aol.moneytracker.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val indonesianLocale = Locale("in", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    /**
     * Format nominal menjadi string Rupiah (misal: "Rp 150.000")
     */
    fun formatRupiah(amount: Long): String {
        val formatted = currencyFormat.format(amount)
        // Standarisasi spasi setelah "Rp"
        return formatted.replace("Rp", "Rp ").replace("Rp  ", "Rp ")
    }

    /**
     * Membersihkan input string menjadi angka Long murni
     */
    fun parseAmount(input: String): Long {
        val cleaned = input.filter { it.isDigit() }
        return cleaned.toLongOrNull() ?: 0L
    }
}
