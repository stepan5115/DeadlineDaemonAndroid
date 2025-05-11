package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.activities.LoadingOverlayHandler
import ru.zuevs5115.deadlinedaemon.activities.LoginActivity
import ru.zuevs5115.deadlinedaemon.api.ApiClient

//profile info updater (request for server)
object ProfileUpdater {
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
                    val response = ApiClient.getInfoService.getInfo(savedUser, savedPass)
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
                        Toast.makeText(context, "Network error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
