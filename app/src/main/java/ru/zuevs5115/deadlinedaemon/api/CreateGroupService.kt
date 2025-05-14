package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface CreateGroupService {
    @FormUrlEncoded
    @POST("createGroup")
    suspend fun createGroup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("groupName") groupName: String
    ): Response<CreateGroupResponse>
}

data class CreateGroupResponse(
    val status: String,
    val message: String
)