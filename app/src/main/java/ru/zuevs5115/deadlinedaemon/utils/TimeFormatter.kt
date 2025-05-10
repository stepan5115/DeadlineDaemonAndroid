package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import ru.zuevs5115.deadlinedaemon.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale


object TimeFormatter {
    fun formatNotificationInterval(seconds: Int?, context: Context): String {
        if (seconds == null) return context.getString(R.string.not_specified)

        val duration = Duration.ofSeconds(seconds.toLong())
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60

        return buildString {
            if (days > 0) append("$days дн. ")
            if (hours > 0) append("$hours час. ")
            if (minutes > 0) append("$minutes мин. ")
            if (secs > 0 && days == 0L && hours == 0L) append("$secs сек.")
        }.trim().ifEmpty { "0 сек." }
    }
    fun formatTimestamp(timestamp: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}