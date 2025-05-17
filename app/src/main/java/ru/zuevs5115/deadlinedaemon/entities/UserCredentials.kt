package ru.zuevs5115.deadlinedaemon.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_credentials", primaryKeys = ["username", "password"])
data class UserCredentials(
    val username: String,
    val password: String
)