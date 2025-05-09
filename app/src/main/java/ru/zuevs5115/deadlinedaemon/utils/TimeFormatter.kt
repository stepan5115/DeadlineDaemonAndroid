package ru.zuevs5115.deadlinedaemon.utils

import java.time.Duration


object TimeFormatter {
    fun formatNotificationInterval(seconds: Int?): String {
        if (seconds == null) return "Не задан"

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
}