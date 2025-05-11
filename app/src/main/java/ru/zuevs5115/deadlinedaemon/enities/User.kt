package ru.zuevs5115.deadlinedaemon.enities

data class User(
    var id: Long,
    var username: String,
    var groups: String,
    var canEditTasks: Boolean,
    var allowNotifications: Boolean,
    var notificationIntervalSeconds: Long,
    var completedAssignments: String,
    var notificationExcludedSubjects: String
)