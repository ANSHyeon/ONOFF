package com.anshyeon.onoff.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatText {

    private const val DATE_YEAR_MONTH_DAY_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private val currentLocale
        get() = SystemConfiguration.currentLocale

    fun getCurrentTime(): String {
        return applyDateFormat()
    }

    private fun applyDateFormat(): String {
        val formatter = SimpleDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN, currentLocale)
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        return formatter.format(currentDate)
    }
}