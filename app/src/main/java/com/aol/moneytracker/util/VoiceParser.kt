package com.aol.moneytracker.util

import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.TransactionType

data class ParsedVoiceTransaction(
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String
)

object VoiceParser {
    fun parse(spokenText: String): ParsedVoiceTransaction {
        val lower = spokenText.lowercase().trim()

        // 1. Determine type
        val isIncome = lower.contains("gaji") || lower.contains("salary") ||
                lower.contains("bonus") || lower.contains("pemasukan") ||
                lower.contains("income") || lower.contains("investasi") ||
                lower.contains("investment") || lower.contains("freelance") ||
                lower.contains("dapat uang") || lower.contains("transfer masuk")
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 2. Extract amount
        var amount = 0L
        val jutaRegex = """(?i)(\d+(?:[.,]\d+)?)\s*(?:juta|jt|million|m\b)""".toRegex()
        val ribuRegex = """(?i)(\d+(?:[.,]\d+)?)\s*(?:ribu|rb|thousand|k\b)""".toRegex()
        val plainDigitsRegex = """(?i)(?:rp|rp\.\s*)?(\d{4,})""".toRegex()

        val jutaMatch = jutaRegex.find(lower)
        val ribuMatch = ribuRegex.find(lower)
        val plainMatch = plainDigitsRegex.find(lower)

        if (jutaMatch != null) {
            val numStr = jutaMatch.groupValues[1].replace(",", ".")
            val num = numStr.toDoubleOrNull() ?: 0.0
            amount = (num * 1_000_000).toLong()
        } else if (ribuMatch != null) {
            val numStr = ribuMatch.groupValues[1].replace(",", ".")
            val num = numStr.toDoubleOrNull() ?: 0.0
            amount = (num * 1_000).toLong()
        } else if (plainMatch != null) {
            amount = plainMatch.groupValues[1].toLongOrNull() ?: 0L
        }

        // Clean title from extracted amount words
        var cleanedTitle = spokenText
            .replace(jutaRegex, "")
            .replace(ribuRegex, "")
            .replace(plainDigitsRegex, "")
            .replace(Regex("""(?i)\b(rp|rupiah|sebesar|seharga|total|bayar|buat|for)\b"""), "")
            .trim()
            .replace(Regex("""\s+"""), " ")

        if (cleanedTitle.isBlank()) {
            cleanedTitle = if (type == TransactionType.INCOME) "Pemasukan Suara" else "Pengeluaran Suara"
        } else {
            cleanedTitle = cleanedTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        // 3. Match Category intelligently
        val categoryList = if (type == TransactionType.INCOME) Category.INCOME_CATEGORIES else Category.EXPENSE_CATEGORIES
        val matchedCat = when {
            lower.contains("makan") || lower.contains("kopi") || lower.contains("sarapan") ||
                    lower.contains("siang") || lower.contains("malam") || lower.contains("snack") ||
                    lower.contains("bakso") || lower.contains("resto") || lower.contains("cafe") ||
                    lower.contains("lunch") || lower.contains("coffee") || lower.contains("dinner") ||
                    lower.contains("breakfast") || lower.contains("food") ->
                categoryList.firstOrNull { it.id == "food" }

            lower.contains("bensin") || lower.contains("motor") || lower.contains("mobil") ||
                    lower.contains("parkir") || lower.contains("gojek") || lower.contains("grab") ||
                    lower.contains("angkot") || lower.contains("kereta") || lower.contains("fuel") ||
                    lower.contains("pertamax") || lower.contains("car") || lower.contains("transport") ->
                categoryList.firstOrNull { it.id == "transport" }

            lower.contains("belanja") || lower.contains("baju") || lower.contains("sepatu") ||
                    lower.contains("mall") || lower.contains("pasar") || lower.contains("supermarket") ||
                    lower.contains("minimarket") || lower.contains("shopping") || lower.contains("groceries") ->
                categoryList.firstOrNull { it.id == "shopping" }

            lower.contains("listrik") || lower.contains("pln") || lower.contains("air") ||
                    lower.contains("wifi") || lower.contains("pulsa") || lower.contains("tagihan") ||
                    lower.contains("pdam") || lower.contains("bill") ->
                categoryList.firstOrNull { it.id == "bills" }

            lower.contains("bioskop") || lower.contains("film") || lower.contains("game") ||
                    lower.contains("steam") || lower.contains("nonton") || lower.contains("jalan") ||
                    lower.contains("movie") ->
                categoryList.firstOrNull { it.id == "entertainment" }

            lower.contains("obat") || lower.contains("dokter") || lower.contains("apotek") ||
                    lower.contains("klinik") || lower.contains("sehat") || lower.contains("health") ->
                categoryList.firstOrNull { it.id == "health" }

            lower.contains("buku") || lower.contains("kursus") || lower.contains("kuliah") ||
                    lower.contains("sekolah") || lower.contains("les") || lower.contains("course") ->
                categoryList.firstOrNull { it.id == "education" }

            lower.contains("gaji") || lower.contains("salary") ->
                categoryList.firstOrNull { it.id == "salary" }

            lower.contains("bonus") || lower.contains("thr") ->
                categoryList.firstOrNull { it.id == "bonus" }

            lower.contains("investasi") || lower.contains("saham") || lower.contains("reksadana") ||
                    lower.contains("crypto") || lower.contains("investment") ->
                categoryList.firstOrNull { it.id == "investment" }

            lower.contains("freelance") || lower.contains("proyek") || lower.contains("klien") ||
                    lower.contains("project") ->
                categoryList.firstOrNull { it.id == "freelance" }

            else -> null
        } ?: categoryList.first()

        return ParsedVoiceTransaction(
            title = cleanedTitle,
            amount = amount,
            type = type,
            categoryId = matchedCat.id,
            categoryName = matchedCat.name
        )
    }
}
