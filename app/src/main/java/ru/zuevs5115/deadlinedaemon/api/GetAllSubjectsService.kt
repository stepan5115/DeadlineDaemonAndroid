package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface GetAllSubjectsService {
    @FormUrlEncoded
    @POST("getAllSubjects")
    suspend fun getAllSubjects(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetAllSubjectsResponse>
}

data class GetAllSubjectsResponse(
    val status: String,
    val message: String
)