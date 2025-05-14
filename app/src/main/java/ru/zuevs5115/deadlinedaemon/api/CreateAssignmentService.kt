package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface CreateAssignmentService {
    @FormUrlEncoded
    @POST("createAssignment")
    suspend fun createAssignment(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("groupsId") groupsId: String,
        @Field("deadline") deadline: String,
        @Field("subjectId") subjectId: String
    ): Response<CreateAssignmentResponse>
}

data class CreateAssignmentResponse(
    val status: String,
    val message: String
)