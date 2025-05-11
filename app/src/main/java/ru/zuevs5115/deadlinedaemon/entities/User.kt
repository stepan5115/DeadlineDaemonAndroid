package ru.zuevs5115.deadlinedaemon.entities

//Entity User for simple work with profile information
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