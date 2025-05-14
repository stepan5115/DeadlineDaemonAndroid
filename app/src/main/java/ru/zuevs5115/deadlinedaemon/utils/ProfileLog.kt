package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.activities.LoadingOverlayHandler
import ru.zuevs5115.deadlinedaemon.api.ApiClient

//class for make logIn/signUp requests and fill sharedPref
object ProfileLog {
    //remember main services
    private val authService = ApiClient.authService
    private val signUpService = ApiClient.signUpService
    //logIn request
    fun logIn(username: String, password: String, activity: Context, listenersSuccess: List<() -> Unit>,
              listenersWrong: List<(String) -> Unit>) {
        val context = activity.applicationContext
        //if allow then show loading overlay
        if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
        //coroutine for async
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //get response
                val response = authService.login(username, password)
                //set coroutine thread to Android Main for edit UI and many things (only main can do that)
                withContext(Dispatchers.Main) {
                    //hide loading overlay if allow and process result
                    if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                    if (response.isSuccessful) {
                        SharedPrefs(context).saveCredentials(username, password)
                        listenersSuccess.forEach { it() }
                    } else {
                        //processing error
                        val errorMessage = ErrorHandler.handleError(response)
                        listenersWrong.forEach { it(errorMessage) }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //hide process bar if allow
                    if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                    listenersWrong.forEach { it(context.getString(R.string.network_error)) }
                }
            }
        }
    }
    //signUp request
    fun signUp(username: String, password: String, activity: Context, listenersSuccess: List<() -> Unit>,
               listenersWrong: List<(String) -> Unit>) {
        val context = activity.applicationContext
        //if allow then show loading overlay
        if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()
        //coroutine for async
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //get response
                val response = signUpService.signUp(username, password)
                //set coroutine thread to Android Main for edit UI and many things (only main can do that)
                withContext(Dispatchers.Main) {
                    //hide loading overlay if allow and process result
                    if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                    if (response.isSuccessful) {
                        SharedPrefs(context).saveCredentials(username, password)
                        listenersSuccess.forEach { it() }
                    } else {
                        //processing error
                        val errorMessage = ErrorHandler.handleError(response)
                        listenersWrong.forEach { it(errorMessage) }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //hide process bar if allow
                    if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                    listenersWrong.forEach { it(context.getString(R.string.network_error)) }
                }
            }
        }
    }
}