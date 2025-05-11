package ru.zuevs5115.deadlinedaemon.api

//ErrorResponse for parsing error response
data class ErrorResponse(
    val status: String,
    val message: String
)