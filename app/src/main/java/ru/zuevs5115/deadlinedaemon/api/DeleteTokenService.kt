package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface DeleteTokenService {
    @FormUrlEncoded
    @POST("deleteToken")
    suspend fun deleteToken(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("tokenId") tokenId: String
    ): Response<DeleteTokenResponse>
}

data class DeleteTokenResponse(
    val status: String,
    val message: String
)