package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface ExitGroupService {
    @FormUrlEncoded
    @POST("exitGroup")
    suspend fun exitGroup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("groupName") groupName: String
    ): Response<ExitGroupResponse>
}

data class ExitGroupResponse(
    val status: String,
    val message: String
)