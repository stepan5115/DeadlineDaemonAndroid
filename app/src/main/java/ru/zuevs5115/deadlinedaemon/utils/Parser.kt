package ru.zuevs5115.deadlinedaemon.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.User

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
                    deadline = assignmentJson.getString("deadline"),
                    subject = assignmentJson.getString("subject"),
                    lastNotificationTime = 0
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
                    deadline = assignmentJson.getString("deadline"),
                    subject = assignmentJson.getString("subject"),
                    lastNotificationTime = 0
                ))
            }
        }
        return result;
    }
}