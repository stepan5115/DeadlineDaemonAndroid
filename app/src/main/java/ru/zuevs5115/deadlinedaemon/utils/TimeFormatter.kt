package ru.zuevs5115.deadlinedaemon.utils

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale

//just time formatter
object TimeFormatter {
    //convert interval to simple format
    fun formatNotificationInterval(seconds: Long): String {
        val duration = Duration.ofSeconds(seconds)
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
    //make date for last update status
    fun formatTimestamp(timestamp: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}