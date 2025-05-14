package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.util.Log
import ru.zuevs5115.deadlinedaemon.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

//just time formatter
object TimeFormatter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val spaceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
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
    fun fromStringToLocalDateTime(time: String?) : LocalDateTime? {
        return try {
            LocalDateTime.parse(time, formatter)
        } catch (e: Throwable) {
            null
        }
    }
    fun fromStringSpaceToLocalDateTime(time: String?) : LocalDateTime? {
        return try {
            LocalDateTime.parse(time, spaceFormatter)
        } catch (e: Throwable) {
            null
        }
    }
}