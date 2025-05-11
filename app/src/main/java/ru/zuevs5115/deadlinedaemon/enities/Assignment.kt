package ru.zuevs5115.deadlinedaemon.enities

data class Assignment(
    var id: Long,
    var title: String,
    var description: String,
    var groups: Set<String>,
    var deadline: String,
    var subjectId: Long,
    var lastNotificationTime: Long = 0
)