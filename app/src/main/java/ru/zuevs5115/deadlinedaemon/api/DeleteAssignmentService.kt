package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface DeleteAssignmentService {
    @FormUrlEncoded
    @POST("deleteAssignment")
    suspend fun deleteAssignment(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("assignmentId") assignmentId: String
    ): Response<DeleteAssignmentResponse>
}

data class DeleteAssignmentResponse(
    val status: String,
    val message: String
)