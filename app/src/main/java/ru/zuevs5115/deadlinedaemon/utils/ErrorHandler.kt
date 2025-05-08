package ru.zuevs5115.deadlinedaemon.utils

import com.google.gson.Gson
import retrofit2.Response
import ru.zuevs5115.deadlinedaemon.api.ErrorResponse

object ErrorHandler {
    // Функция для обработки ошибки
    fun handleError(response: Response<*>): String {
        return if (response.isSuccessful) {
            // Если ответ успешен, просто возвращаем сообщение
            "OK"
        } else {
            // Если ответ не успешен, извлекаем errorBody и парсим его
            val errorBody = response.errorBody()?.string()
            return try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message // Возвращаем сообщение из ошибки
            } catch (e: Exception) {
                "Неизвестная ошибка" // Если не удается распарсить ошибку, возвращаем общий текст
            }
        }
    }
}
