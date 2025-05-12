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

object ProfileEditor {
    private val completeAssignmentService = ApiClient.completeAssignmentService
    private val inCompleteAssignmentService = ApiClient.inCompleteAssignmentService
    private val setNotificationStatusService = ApiClient.setNotificationStatusService
    private val getAdminRightsService = ApiClient.getAdminRightsService
    private val setIntervalService = ApiClient.setIntervalService
    private val excludeSubjectService = ApiClient.excludeSubjectService
    private val includeSubjectService = ApiClient.includeSubjectService

    //complete assignment
    fun completeAssignment(assignmentId: String, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = completeAssignmentService.completeAssignment(savedUser, savedPass, assignmentId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun inCompleteAssignment(assignmentId: String, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = inCompleteAssignmentService.completeAssignment(savedUser, savedPass, assignmentId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun setNotificationStatus(newStatus: Boolean, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = setNotificationStatusService.setNotificationStatus(savedUser, savedPass, newStatus)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun getAdminRights(token: String, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAdminRightsService.getAdminRights(savedUser, savedPass, token)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun setIntervalService(seconds: Long, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = setIntervalService.setInterval(savedUser, savedPass, seconds)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun excludeSubject(subjectId: String, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = excludeSubjectService.excludeSubject(savedUser, savedPass, subjectId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun includeSubject(subjectId: String, activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = includeSubjectService.includeSubject(savedUser, savedPass, subjectId)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            //toast about success
                            //Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it() }
                        } else {
                            //make toast about error
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}