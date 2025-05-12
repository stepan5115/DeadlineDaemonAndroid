package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface GetAllGroupsService {
    @FormUrlEncoded
    @POST("getAllGroups")
    suspend fun getAllGroups(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetAllGroupsResponse>
}

data class GetAllGroupsResponse(
    val status: String,
    val message: String
)