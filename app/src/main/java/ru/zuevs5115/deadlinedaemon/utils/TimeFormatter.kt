package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.util.Log
import ru.zuevs5115.deadlinedaemon.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale

//just time formatter
object TimeFormatter {
    //convert interval to simple format
    fun formatNotificationInterval(seconds: Long, context: Context): String {
        val duration = Duration.ofSeconds(seconds)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60

        return buildString {
            if (days > 0) append(context.getString(R.string.days_short_pc, days.toString())).append(" ")
            if (hours > 0) append(context.getString(R.string.hours_short_pc, hours.toString())).append(" ")
            if (minutes > 0) append(context.getString(R.string.minutes_short_pc, minutes.toString())).append(" ")
            if (secs > 0 && days == 0L && hours == 0L)
                append(context.getString(R.string.seconds_short_pc, secs.toString()))
        }.trim().ifEmpty { context.getString(R.string.seconds_short_pc, "0") }
    }
    fun parseIntervalToSeconds(formattedString: String, context: Context): Long {
        var seconds = 0L
        val parts = formattedString.split(" ")

        var i = 0
        while (i < parts.size) {
            val value = parts[i].toLongOrNull() ?: 0L
            when {
                i + 1 >= parts.size -> break
                parts[i+1] == context.getString(R.string.days_short) -> seconds += value * 86400
                parts[i+1] == context.getString(R.string.hours_short) -> seconds += value * 3600
                parts[i+1] == context.getString(R.string.minutes_short) -> seconds += value * 60
                parts[i+1] == context.getString(R.string.seconds_short) -> seconds += value
            }
            i += 2
        }
        return seconds
    }
    fun fastMaxPartOfInterval(formattedString: String, context: Context): Pair<Long, Int> {
        val timeUnits = context.resources.getStringArray(R.array.time_units)
        val parts = formattedString.split(" ")
        Log.d("HAHAHHA", timeUnits.indexOf(parts[1]).toString())
        Log.d("HAHAHHA", parts[1])
        return (parts[0].toLongOrNull() ?: 0L) to timeUnits.indexOf(parts[1])
    }
    //make date for last update status
    fun formatTimestamp(timestamp: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        return format.format(Date(timestamp))
    }
    fun extractLargestTimeUnit(seconds: Long, context: Context): Pair<Long, Int> {
        val timeUnits = context.resources.getStringArray(R.array.time_units)

        return when {
            seconds >= 86400 -> seconds / 86400 to timeUnits.indexOf("дн.")
            seconds >= 3600 -> seconds / 3600 to timeUnits.indexOf("час.")
            seconds >= 60 -> seconds / 60 to timeUnits.indexOf("мин.")
            else -> seconds to timeUnits.indexOf("сек.")
        }
    }

}