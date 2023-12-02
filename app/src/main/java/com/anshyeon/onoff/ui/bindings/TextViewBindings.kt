package com.anshyeon.onoff.ui.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.anshyeon.onoff.R
import com.anshyeon.onoff.util.DateFormatText

@BindingAdapter("elapsedTimeFormat")
fun TextView.setElapsedTime(dateString: String) {
    val publishedDate = DateFormatText.convertToDate(dateString)
    val currentDate = DateFormatText.getCurrentDate()

    val seconds = (currentDate.time - publishedDate.time) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val month = days / 30

    text = when {
        minutes < 1 -> context.getString(R.string.elapsed_time_seconds, seconds)

        hours < 1 -> context.getString(R.string.elapsed_time_minutes, minutes)

        days < 1 -> context.getString(R.string.elapsed_time_hours, hours)

        weeks < 1 -> context.getString(R.string.elapsed_time_days, days)

        month < 1 -> context.getString(R.string.elapsed_time_weeks, weeks)

        else -> context.getString(R.string.elapsed_time_months, month)
    }
}