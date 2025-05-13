package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface GetAllSubjectsIndependenceUserService {
    @FormUrlEncoded
    @POST("getAllSubjectsIndependenceUser")
    suspend fun getAllSubjectsIndependenceUserService(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetAllSubjectsIndependenceUserResponse>
}

data class GetAllSubjectsIndependenceUserResponse(
    val status: String,
    val message: String
)