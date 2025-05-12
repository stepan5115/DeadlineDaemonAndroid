package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface SetNotificationStatusService {
    @FormUrlEncoded
    @POST("setNotificationStatus")
    suspend fun setNotificationStatus(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("allowNotifications") allowNotifications: Boolean
    ): Response<SetNotificationStatusResponse>
}

data class SetNotificationStatusResponse(
    val status: String,
    val message: String
)