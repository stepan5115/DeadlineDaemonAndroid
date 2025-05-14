package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context

//shared preferences
class SharedPrefs(context: Context) {
    //like static val (maximum antiquity of data)
    companion object {
        private const val UPDATE_INTERVAL = 5 * 60 * 1000L
    }
    //prefs
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    //remember context to make request
    private val context = context.applicationContext
    //save login information
    fun saveCredentials(username: String, password: String) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }
    //save response from server
    fun saveInfo(jsonInfo: String) {
        prefs.edit().apply {
            putString("info", jsonInfo)
            apply()
        }
    }
    //get last response of server
    fun getInfo() : String? {
        val lastUpdate = prefs.getLong("lastUpdate", 0)
        val currentTime = System.currentTimeMillis()
        //if data too old then update
        if (currentTime - lastUpdate > UPDATE_INTERVAL) {
            ProfileUpdater.updateProfileData(context, listOf())
            //update lastUpdate time
            prefs.edit().apply {
                putLong("lastUpdate", currentTime)
                apply()
            }
            //return information
            return prefs.getString("info", null)
        }
        //return information
        return prefs.getString("info", null)
    }
    //save last response time
    fun saveLastUpdate(lastUpdate: Long) {
        prefs.edit().apply {
            putLong("lastUpdate", lastUpdate)
            apply()
        }
    }
    //get last response time
    fun getLastUpdate() : Long {
        return prefs.getLong("lastUpdate", 0)
    }
    //get login information
    fun getCredentials(): Pair<String?, String?> {
        return Pair(
            prefs.getString("username", null),
            prefs.getString("password", null)
        )
    }
    fun saveSubjects(subjects: String) {
        prefs.edit().apply {
            putString("subjects", subjects)
            apply()
        }
    }
    fun getSubjects() : String? {
        return prefs.getString("subjects", null)
    }
    fun getAllSubjects() : String? {
        return prefs.getString("allSubjects", null)
    }
    fun saveAllSubjects(allSubjects: String) {
        prefs.edit().apply {
            putString("allSubjects", allSubjects)
            apply()
        }
    }
    fun saveGroups(groups: String) {
        prefs.edit().apply {
            putString("groups", groups)
            apply()
        }
    }
    fun getGroups() : String? {
        return prefs.getString("groups", null)
    }
    fun getAllGroups() : String? {
        return prefs.getString("allGroups", null)
    }
    fun saveAllGroups(allSubjects: String) {
        prefs.edit().apply {
            putString("allGroups", allSubjects)
            apply()
        }
    }
    fun getAllAssignments() : String? {
        return prefs.getString("allAssignments", null)
    }
    fun saveAllAssignments(allSubjects: String) {
        prefs.edit().apply {
            putString("allAssignments", allSubjects)
            apply()
        }
    }
    fun saveTokens(tokens: String) {
        prefs.edit().apply {
            putString("tokens", tokens)
            apply()
        }
    }
    fun getTokens(): String? {
        return prefs.getString("tokens", null)
    }
    //clear allInformation (if you exit)
    fun clearInformation() {
        with(prefs.edit()) {
            remove("username")
            remove("password")
            remove("info")
            remove("lastUpdate")
            remove("subjects")
            remove("groups")
            remove("allSubjects")
            remove("allGroups")
            remove("allAssignments")
            apply()
        }
    }
}