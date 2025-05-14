package ru.zuevs5115.deadlinedaemon.entities

import java.time.LocalDateTime

//Entity Assignment for simple work with assignment information
data class Assignment(
    var id: Long,
    var title: String,
    var description: String,
    var groups: Set<String>,
    var deadline: LocalDateTime,
    var subject: String,
    var lastNotificationTime: Long = 0
)