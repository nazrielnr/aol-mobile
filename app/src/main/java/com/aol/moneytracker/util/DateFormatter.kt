package com.aol.moneytracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object DateFormatter {
    private var cachedLocale: java.util.Locale? = null
    private var timeFormatter: SimpleDateFormat? = null
    private var fullDateFormatter: SimpleDateFormat? = null
    private var dayMonthFormatter: SimpleDateFormat? = null

    @Synchronized
    private fun ensureFormatters(locale: java.util.Locale) {
        if (cachedLocale != locale || timeFormatter == null) {
            cachedLocale = locale
            timeFormatter = SimpleDateFormat("HH:mm", locale)
            fullDateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
            dayMonthFormatter = SimpleDateFormat("dd MMMM yyyy", locale)
        }
    }

    @Synchronized
    fun formatFriendly(timeMillis: Long): String {
        val locale = LanguageManager.getActiveLocale()
        ensureFormatters(locale)
        val isEnglish = LanguageManager.currentLanguage.value == AppLanguage.ENGLISH
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

        return when {
            isSameDay(now, target) -> {
                val prefix = if (isEnglish) "Today" else "Hari ini"
                "$prefix, ${timeFormatter!!.format(Date(timeMillis))}"
            }
            isYesterday(now, target) -> {
                val prefix = if (isEnglish) "Yesterday" else "Kemarin"
                "$prefix, ${timeFormatter!!.format(Date(timeMillis))}"
            }
            else -> fullDateFormatter!!.format(Date(timeMillis))
        }
    }

    @Synchronized
    fun formatTime(timeMillis: Long): String {
        val locale = LanguageManager.getActiveLocale()
        ensureFormatters(locale)
        return timeFormatter!!.format(Date(timeMillis))
    }

    @Synchronized
    fun formatDateOnly(timeMillis: Long): String {
        val locale = LanguageManager.getActiveLocale()
        ensureFormatters(locale)
        return dayMonthFormatter!!.format(Date(timeMillis))
    }

    @Synchronized
    fun formatDateGroup(timeMillis: Long): String {
        val locale = LanguageManager.getActiveLocale()
        ensureFormatters(locale)
        val isEnglish = LanguageManager.currentLanguage.value == AppLanguage.ENGLISH
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

        return when {
            isSameDay(now, target) -> if (isEnglish) "Today" else "Hari ini"
            isYesterday(now, target) -> if (isEnglish) "Yesterday" else "Kemarin"
            else -> dayMonthFormatter!!.format(Date(timeMillis))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, target: Calendar): Boolean {
        val cloneNow = now.clone() as Calendar
        cloneNow.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(cloneNow, target)
    }
}
