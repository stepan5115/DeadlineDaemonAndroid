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

//profile info updater (request for server)
object ProfileUpdater {
    private val getInfoService = ApiClient.getInfoService
    private val getAllSubjectsService = ApiClient.getAllSubjectsService

    fun updateProfileData(activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getInfoService.getInfo(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val lastUpdate = System.currentTimeMillis()
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveInfo(responseText)
                            SharedPrefs(context).saveLastUpdate(lastUpdate)
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
                        Toast.makeText(context, context.getString(R.string.network_error, ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun getAllSubjects(activity: Context, listeners: List<() -> Unit>) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show loading if allow
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //request
                    val response = getAllSubjectsService.getAllSubjects(savedUser, savedPass)
                    //set to amin thread to make Toasts
                    withContext(Dispatchers.Main) {
                        //hide loading if allow
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        //success
                        if (response.isSuccessful) {
                            //set lastUpdate and info
                            val lastUpdateSubject = System.currentTimeMillis()
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveSubjects(responseText)
                            SharedPrefs(context).saveSubjectsLastUpdate(lastUpdateSubject)
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
                        Toast.makeText(context, context.getString(R.string.network_error, ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
