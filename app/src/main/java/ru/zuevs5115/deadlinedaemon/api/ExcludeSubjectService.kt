package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface ExcludeSubjectService {
    @FormUrlEncoded
    @POST("excludeSubject")
    suspend fun excludeSubject(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("subjectId") subjectId: String
    ): Response<ExcludeSubjectResponse>
}

data class ExcludeSubjectResponse(
    val status: String,
    val message: String
)