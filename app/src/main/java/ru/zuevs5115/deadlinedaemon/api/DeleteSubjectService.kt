package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface DeleteSubjectService {
    @FormUrlEncoded
    @POST("deleteSubject")
    suspend fun deleteSubject(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("subjectId") subjectId: String
    ): Response<DeleteSubjectResponse>
}

data class DeleteSubjectResponse(
    val status: String,
    val message: String
)