package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface DeleteGroupService {
    @FormUrlEncoded
    @POST("deleteGroup")
    suspend fun deleteGroup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("groupId") groupId: String
    ): Response<DeleteGroupResponse>
}

data class DeleteGroupResponse(
    val status: String,
    val message: String
)