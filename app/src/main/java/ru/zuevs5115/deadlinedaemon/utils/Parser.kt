package ru.zuevs5115.deadlinedaemon.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.zuevs5115.deadlinedaemon.entities.AdminToken
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.entities.User
import java.sql.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//parse information (for last response parsing)
object Parser {
    //make user from json
    fun fromJsonToUser(jsonString: String) : User {
        val json = JSONObject(jsonString)
        val id : Int = json.getInt("user_id")
        val username : String = json.getString("username")
        val groups = json.getJSONArray("groups").let { array ->
            (0 until array.length()).joinToString(",") {
                array.getJSONObject(it).getString("name")
            }
        }
        val canEditTasks : Boolean = json.getBoolean("canEditTasks")
        val allowNotifications : Boolean = json.getBoolean("allowNotifications")
        val notificationIntervalSeconds : Int = json.getInt("notificationIntervalSeconds")
        val notificationExcludedSubjects = json.getJSONArray("notificationExcludedSubjects").let { array ->
            (0 until array.length()).joinToString(",") {
                array.getJSONObject(it).getString("name")
            }
        }
        val completedAssignments = json.getJSONArray("completedAssignments").let { array ->
            (0 until array.length()).joinToString(",") {
                array.getJSONObject(it).getString("title")
            }
        }
        return User(id.toLong(), username, groups, canEditTasks, allowNotifications,
            notificationIntervalSeconds.toLong(), completedAssignments, notificationExcludedSubjects)
    }
    //make assignments list from json
    fun fromJsonToAssignments(jsonString: String) : Set<Assignment> {
        val json = JSONObject(jsonString)
        val result : MutableSet<Assignment> = HashSet()
        if (json.has("assignments")) {
            val jsonArray : JSONArray = json.getJSONArray("assignments")
            for (i in 0 until jsonArray.length()) {
                val assignmentJson = jsonArray.getJSONObject(i)
                result.add(Assignment(
                    id = assignmentJson.getLong("assignment_id"),
                    title = assignmentJson.getString("title"),
                    description = assignmentJson.getString("description"),
                    groups = assignmentJson.getJSONArray("groups").let { array ->
                        (0 until array.length()).mapTo(mutableSetOf()) { array.getString(it) }
                    },
                    deadline = TimeFormatter.fromStringToLocalDateTime(assignmentJson.getString("deadline"))!!,
                    subject = assignmentJson.getString("subject"),
                    lastNotificationTime = 0
                ))
            }
        }
        return result
    }
    fun fromJsonToSubjects(jsonString: String) : Set<Subject> {
        val json = JSONObject(jsonString)
        val result : MutableSet<Subject> = HashSet()
        if (json.has("subjects")) {
            val jsonArray : JSONArray = json.getJSONArray("subjects")
            for (i in 0 until jsonArray.length()) {
                val subjectJson = jsonArray.getJSONObject(i)
                result.add(Subject(
                    id = subjectJson.getLong("subject_id"),
                    name = subjectJson.getString("name"),
                ))
            }
        }
        return result
    }
    fun fromJsonToGroups(jsonString: String) : Set<Group> {
        val json = JSONObject(jsonString)
        val result : MutableSet<Group> = HashSet()
        if (json.has("groups")) {
            val jsonArray : JSONArray = json.getJSONArray("groups")
            for (i in 0 until jsonArray.length()) {
                val groupJson = jsonArray.getJSONObject(i)
                result.add(Group(
                    id = groupJson.getLong("group_id"),
                    name = groupJson.getString("name"),
                ))
            }
        }
        return result;
    }
    //sync assignments (remove disappeared, add new)
    fun synchronizeAssignments(oldResult: MutableSet<Assignment>, newResult: Set<Assignment>) {
        val hashTmpNew : MutableMap<Long, Assignment> = HashMap()
        val hashTmpOld : MutableMap<Long, Assignment> = HashMap()
        for (oldAssignment: Assignment in oldResult)
            hashTmpOld[oldAssignment.id] = oldAssignment
        for (newAssignment: Assignment in newResult) {
            hashTmpNew[newAssignment.id] = newAssignment
            if (!hashTmpOld.containsKey(newAssignment.id))
                oldResult.add(newAssignment)
        }
        oldResult.filter { hashTmpNew.containsKey(it.id) }
    }
    //get completed assignments
    fun getCompletedAssignments(jsonString: String): Set<Assignment> {
        val json = JSONObject(jsonString)
        val result : MutableSet<Assignment> = HashSet()
        if (json.has("completedAssignments")) {
            val jsonArray : JSONArray = json.getJSONArray("completedAssignments")
            for (i in 0 until jsonArray.length()) {
                val assignmentJson = jsonArray.getJSONObject(i)
                result.add(Assignment(
                    id = assignmentJson.getLong("assignment_id"),
                    title = assignmentJson.getString("title"),
                    description = assignmentJson.getString("description"),
                    groups = assignmentJson.getJSONArray("groups").let { array ->
                        (0 until array.length()).mapTo(mutableSetOf()) { array.getString(it) }
                    },
                    deadline = TimeFormatter.fromStringToLocalDateTime(assignmentJson.getString("deadline"))!!,
                    subject = assignmentJson.getString("subject"),
                    lastNotificationTime = 0
                ))
            }
        }
        return result
    }
    fun getExcludeSubjects(jsonString: String): Set<Subject> {
        val json = JSONObject(jsonString)
        val result : MutableSet<Subject> = HashSet()
        if (json.has("notificationExcludedSubjects")) {
            val jsonArray : JSONArray = json.getJSONArray("notificationExcludedSubjects")
            for (i in 0 until jsonArray.length()) {
                val subjectJson = jsonArray.getJSONObject(i)
                result.add(Subject(
                    id = subjectJson.getLong("subject_id"),
                    name = subjectJson.getString("name"),
                ))
            }
        }
        return result
    }
    fun getAdminTokens(jsonString: String): Set<AdminToken> {
        val json = JSONObject(jsonString)
        val result : MutableSet<AdminToken> = HashSet()
        if (json.has("tokens")) {
            val jsonArray : JSONArray = json.getJSONArray("tokens")
            for (i in 0 until jsonArray.length()) {
                val subjectJson = jsonArray.getJSONObject(i)
                result.add(
                    AdminToken(
                    id = subjectJson.getLong("token_id"),
                    token = subjectJson.getString("token")
                    )
                )
            }
        }
        return result
    }
    fun isHaveAdminRight(jsonString: String?): Boolean {
        if (jsonString == null)
            return false
        try {
            val user = fromJsonToUser(jsonString)
            return user.canEditTasks
        } catch (e: Throwable) {
            return false
        }
    }
    // Сериализация List<String> в JSON строку
    fun groupNamesToJson(groupNames: List<String>): String {
        return JSONArray().apply {
            groupNames.forEach { put(it) }
        }.toString()
    }
}