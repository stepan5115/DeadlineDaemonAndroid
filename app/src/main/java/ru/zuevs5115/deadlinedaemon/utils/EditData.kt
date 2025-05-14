package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.activities.LoadingOverlayHandler
import ru.zuevs5115.deadlinedaemon.activities.LoginActivity
import ru.zuevs5115.deadlinedaemon.api.ApiClient

object EditData {
    private val deleteAssignmentService = ApiClient.deleteAssignmentService
    private val createAssignmentService = ApiClient.createAssignmentService

    fun deleteAssignmentService(activity: Context, listeners: List<() -> Unit>, assignmentId: String) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = deleteAssignmentService.deleteAssignment(savedUser, savedPass, assignmentId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error_ph, ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun createAssignmentService(activity: Context, listeners: List<() -> Unit>, title: String,
                                description: String, groupNames: String, deadline: String,
                                subjectId: String) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = createAssignmentService.createAssignment(savedUser, savedPass, title,
                        description, groupNames, deadline, subjectId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error_ph, ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}