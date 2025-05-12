package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//Service for getInfo request
interface GetAdminRightsService {
    @FormUrlEncoded
    @POST("getAdminRights")
    suspend fun getAdminRights(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("token") token: String
    ): Response<GetAdminRightsResponse>
}

data class GetAdminRightsResponse(
    val status: String,
    val message: String
)