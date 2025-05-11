package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface GetInfoService {
    @FormUrlEncoded
    @POST("getInfo")
    suspend fun getInfo(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<GetInfoResponse>
}

data class GetInfoResponse(
    val status: String,
    val message: String
)