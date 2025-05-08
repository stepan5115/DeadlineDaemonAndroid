package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context

class SharedPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveCredentials(username: String, password: String) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun getCredentials(): Pair<String?, String?> {
        return Pair(
            prefs.getString("username", null),
            prefs.getString("password", null)
        )
    }
}