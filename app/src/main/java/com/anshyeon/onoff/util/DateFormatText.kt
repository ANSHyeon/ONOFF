package com.anshyeon.onoff.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatText {

    private const val DATE_YEAR_MONTH_DAY_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val DATE_FILE_NAME_PATTERN = "yyyyMMdd_HHmmss"
    private val currentLocale
        get() = SystemConfiguration.currentLocale

    fun convertToDate(dateString: String): Date {
        return SimpleDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN, currentLocale).parse(dateString)
            ?: Date()
    }

    fun getCurrentTime(): String {
        return applyDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN)
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
    }

    fun getFileNameFormat(): String {
        return applyDateFormat(DATE_FILE_NAME_PATTERN)
    }

    private fun applyDateFormat(pattern: String): String {
        val formatter = SimpleDateFormat(pattern, currentLocale)
        val currentDate = getCurrentDate()
        return formatter.format(currentDate)
    }
}