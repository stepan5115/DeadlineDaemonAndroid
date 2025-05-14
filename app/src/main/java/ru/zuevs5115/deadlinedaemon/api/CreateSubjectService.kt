package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface CreateSubjectService {
    @FormUrlEncoded
    @POST("createSubject")
    suspend fun createSubject(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("subjectName") subjectName: String
    ): Response<CreateSubjectResponse>
}

data class CreateSubjectResponse(
    val status: String,
    val message: String
)