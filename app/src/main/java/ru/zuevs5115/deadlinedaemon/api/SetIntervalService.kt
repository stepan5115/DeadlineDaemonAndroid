package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface SetIntervalService {
    @FormUrlEncoded
    @POST("setInterval")
    suspend fun setInterval(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("seconds") seconds: Long
    ): Response<SetIntervalResponse>
}

data class SetIntervalResponse(
    val status: String,
    val message: String
)