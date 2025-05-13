package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface GetAllGroupsIndependenceUserService {
    @FormUrlEncoded
    @POST("getAllGroupsIndependenceUser")
    suspend fun getAllGroupsIndependenceUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetAllGroupsIndependenceUserResponse>
}

data class GetAllGroupsIndependenceUserResponse(
    val status: String,
    val message: String
)