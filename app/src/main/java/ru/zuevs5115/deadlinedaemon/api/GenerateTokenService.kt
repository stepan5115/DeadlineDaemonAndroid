package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for login request
interface GenerateTokenService {
    @FormUrlEncoded
    @POST("generateToken")
    suspend fun generateToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GenerateTokenResponse>
}

data class GenerateTokenResponse(
    val status: String,
    val message: String
)