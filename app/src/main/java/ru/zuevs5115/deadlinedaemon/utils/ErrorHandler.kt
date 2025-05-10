package ru.zuevs5115.deadlinedaemon.utils

import com.google.gson.Gson
import retrofit2.Response
import ru.zuevs5115.deadlinedaemon.api.ErrorResponse

object ErrorHandler {
    //Error process
    fun handleError(response: Response<*>): String {
        return if (response.isSuccessful) {
            //If success
            "OK"
        } else {
            //Get response body
            val errorBody = response.errorBody()?.string()
            return try {
                //parse and return message
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message
            } catch (e: Exception) {
                "Unknown error"
            }
        }
    }
}
