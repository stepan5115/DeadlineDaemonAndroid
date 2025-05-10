package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://192.168.50.69:8080/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService
        get() = retrofit.create(AuthService::class.java)
    val signUpService: SignUpService
        get() = retrofit.create(SignUpService::class.java)
    val getInfoService: GetInfoService
        get() = retrofit.create(GetInfoService::class.java)
}