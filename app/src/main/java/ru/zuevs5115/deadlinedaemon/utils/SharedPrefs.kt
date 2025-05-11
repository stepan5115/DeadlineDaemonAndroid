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

    fun saveInfo(jsonInfo: String) {
        prefs.edit().apply {
            putString("info", jsonInfo)
            apply()
        }
    }

    fun getInfo() : String? {
        return prefs.getString("info", null)
    }

    fun saveLastUpdate(lastUpdate: Long) {
        prefs.edit().apply {
            putLong("lastUpdate", lastUpdate)
            apply()
        }
    }

    fun getLastUpdate() : Long {
        return prefs.getLong("lastUpdate", 0)
    }

    fun getCredentials(): Pair<String?, String?> {
        return Pair(
            prefs.getString("username", null),
            prefs.getString("password", null)
        )
    }

    fun clearCredentials() {
        with(prefs.edit()) {
            remove("username")
            remove("password")
            apply()
        }
    }
}