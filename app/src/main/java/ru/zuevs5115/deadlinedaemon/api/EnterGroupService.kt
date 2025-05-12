package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface EnterGroupService {
    @FormUrlEncoded
    @POST("enterGroup")
    suspend fun enterGroup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("groupName") groupName: String
    ): Response<EnterGroupResponse>
}

data class EnterGroupResponse(
    val status: String,
    val message: String
)