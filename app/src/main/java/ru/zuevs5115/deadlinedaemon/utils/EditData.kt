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
    private val generateTokenService = ApiClient.generateTokenService
    private val deleteTokenService = ApiClient.deleteTokenService
    private val createSubjectService = ApiClient.createSubjectService
    private val deleteSubjectService = ApiClient.deleteSubjectService
    private val createGroupService = ApiClient.createGroupService
    private val deleteGroupService = ApiClient.deleteGroupService

    fun deleteAssignmentService(activity: Context, listeners: List<() -> Unit>, assignmentId: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = deleteAssignmentService.deleteAssignment(
                            savedUser,
                            savedPass,
                            assignmentId
                        )
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun createAssignmentService(activity: Context, listeners: List<() -> Unit>, title: String,
                                description: String, groupNames: String, deadline: String,
                                subjectId: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = createAssignmentService.createAssignment(
                            savedUser, savedPass, title,
                            description, groupNames, deadline, subjectId
                        )
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun generateTokenService(activity: Context, listeners: List<(String) -> Unit>) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = generateTokenService.generateToken(savedUser, savedPass)
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //success
                            if (response.isSuccessful) {
                                listeners.forEach { it(response.body()?.message ?: "") }
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun deleteToken(activity: Context, listeners: List<() -> Unit>, tokenId: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = deleteTokenService.deleteToken(savedUser, savedPass, tokenId)
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun createSubject(activity: Context, listeners: List<() -> Unit>, subjectName: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = createSubjectService.createSubject(savedUser, savedPass, subjectName)
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun deleteSubject(activity: Context, listeners: List<() -> Unit>, subjectId: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = deleteSubjectService.deleteSubject(savedUser, savedPass, subjectId)
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun createGroup(activity: Context, listeners: List<() -> Unit>, groupName: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = createGroupService.createGroup(savedUser, savedPass, groupName)
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
    fun deleteGroup(activity: Context, listeners: List<() -> Unit>, groupId: String) {
        if (Parser.isHaveAdminRight(SharedPrefs(activity).getInfo())) {
            val context = activity.applicationContext
            val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
            if (savedUser != null && savedPass != null) {
                //show loading if allow
                if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
                //coroutine for async
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        //request
                        val response = EditData.deleteGroupService.deleteGroup(savedUser, savedPass, groupId)
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
                            }
                        }
                    } catch (e: Exception) {
                        //set to amin thread to make Toasts
                        withContext(Dispatchers.Main) {
                            //hide loading if allow
                            if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                            //make toast about error
                            Toast.makeText(
                                context,
                                context.getString(R.string.network_error_ph, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else
            Toast.makeText(
                activity,
                activity.getString(R.string.have_not_rights),
                Toast.LENGTH_SHORT
            ).show()
    }
}