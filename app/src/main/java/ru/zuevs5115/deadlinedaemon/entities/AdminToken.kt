package ru.zuevs5115.deadlinedaemon.entities

import java.time.LocalDateTime

//Entity Assignment for simple work with assignment information
data class AdminToken(
    var id: Long,
    var token: String
)