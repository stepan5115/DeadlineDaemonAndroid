package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SignUpService {
    @FormUrlEncoded
    @POST("signup")
    suspend fun signUp(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<SignUpResponse>
}

data class SignUpResponse(
    val status: String,
    val message: String
)