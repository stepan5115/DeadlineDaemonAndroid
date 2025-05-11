package ru.zuevs5115.deadlinedaemon.entities

//Entity Assignment for simple work with assignment information
data class Assignment(
    var id: Long,
    var title: String,
    var description: String,
    var groups: Set<String>,
    var deadline: String,
    var subject: String,
    var lastNotificationTime: Long = 0
)