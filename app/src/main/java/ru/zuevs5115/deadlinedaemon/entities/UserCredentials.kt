package ru.zuevs5115.deadlinedaemon.entities

import androidx.room.Entity

@Entity(tableName = "user_credentials", primaryKeys = ["username", "password"])
data class UserCredentials(
    val username: String,
    val password: String
)