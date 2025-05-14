package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface GetTokensService {
    @FormUrlEncoded
    @POST("getTokens")
    suspend fun getTokens(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetTokensResponse>
}

data class GetTokensResponse(
    val status: String,
    val message: String
)