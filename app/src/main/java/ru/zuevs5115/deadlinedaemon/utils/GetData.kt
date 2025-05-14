package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.activities.LoadingOverlayHandler
import ru.zuevs5115.deadlinedaemon.activities.LoginActivity
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.DialogCreateAssignmentBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogDeleteAssignmentBinding
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import java.time.LocalDateTime

object GetData {
    private val getAllAssignmentsIndependenceUserService = ApiClient.getAllAssignmentsIndependenceUserService
    private val getAllSubjectsIndependenceUserService = ApiClient.getAllSubjectsIndependenceUserService
    private val getAllGroupsIndependenceUserService = ApiClient.getAllGroupsIndependenceUserService

    fun getAllGroupsIndependenceUserForAssignmentContext(activity: Context, listeners: List<(MutableList<Group>) -> Unit>,
                                     selectedGroups: MutableList<Group>,
                                     binding: ViewBinding
    ) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (binding is DialogCreateAssignmentBinding)
                binding.dialogLoadingOverlay.visibility = View.VISIBLE
            if (binding is DialogDeleteAssignmentBinding)
                binding.dialogLoadingOverlay.visibility = View.VISIBLE
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAllGroupsIndependenceUserService.getAllGroupsIndependenceUser(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (binding is DialogCreateAssignmentBinding)
                            binding.dialogLoadingOverlay.visibility = View.GONE
                        if (binding is DialogDeleteAssignmentBinding)
                            binding.dialogLoadingOverlay.visibility = View.GONE
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveAllGroups(responseText)
                            //toast about success
                            //Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it(selectedGroups) }
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
                        if (binding is DialogCreateAssignmentBinding)
                            binding.dialogLoadingOverlay.visibility = View.GONE
                        if (binding is DialogDeleteAssignmentBinding)
                            binding.dialogLoadingOverlay.visibility = View.GONE
                        //make toast about error
                        Toast.makeText(context, context.getString(R.string.network_error_ph, ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun getAllGroupsIndependenceUserFor(activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAllGroupsIndependenceUserService.getAllGroupsIndependenceUser(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveAllGroups(responseText)
                            //toast about success
                            //Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                            //do what user want
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

    fun getAllSubjectsIndependenceUser(activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAllSubjectsIndependenceUserService.getAllSubjectsIndependenceUserService(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveAllSubjects(responseText)
                            //toast about success
                            //Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                            //do what user want
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
    fun getAllAssignmentsIndependenceUserDeleteContext(activity: Context, listeners: List<(
           String?, String?, Subject?, List<Group>, deadline: LocalDateTime?) -> Unit>,
           title: String?, description: String?, subject: Subject?, groups: List<Group>, deadline: LocalDateTime?) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAllAssignmentsIndependenceUserService.getAllAssignmentsIndependenceUser(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveAllAssignments(responseText)
                            //toast about success
                            //Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                            //do what user want
                            listeners.forEach { it(title, description, subject, groups, deadline) }
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