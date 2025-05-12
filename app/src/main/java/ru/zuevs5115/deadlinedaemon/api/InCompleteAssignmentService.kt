package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface InCompleteAssignmentService {
    @FormUrlEncoded
    @POST("inCompleteAssignment")
    suspend fun completeAssignment(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("assignmentId") assignmentId: String
    ): Response<LoginResponse>
}

data class InCompleteAssignmentResponse(
    val status: String,
    val message: String
)