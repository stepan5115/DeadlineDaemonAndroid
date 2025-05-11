package ru.zuevs5115.deadlinedaemon.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.activities.LoadingOverlayHandler
import ru.zuevs5115.deadlinedaemon.activities.LoginActivity
import ru.zuevs5115.deadlinedaemon.activities.ProfileInfoActivity
import ru.zuevs5115.deadlinedaemon.api.ApiClient

object ProfileUpdater {
    const val ACTION_PROFILE_UPDATED = "PROFILE_UPDATED"
    private var updateHandler: Handler? = null
    private var updateRunnable: Runnable? = null
    private const val UPDATE_INTERVAL = 5 * 60 * 1000L
    private var isRunning = false
    private var listeners = mutableListOf<() -> Unit>()

    fun start(context: Context, listeners: MutableList<() -> Unit>) {
        if (isRunning) return

        updateHandler = Handler(Looper.getMainLooper())
        this.listeners = listeners
        updateRunnable = object : Runnable {
            override fun run() {
                updateProfileData(context)
                updateHandler?.postDelayed(this, UPDATE_INTERVAL)
            }
        }
        updateHandler?.post(updateRunnable!!)
        isRunning = true
    }

    fun stop() {
        updateHandler?.removeCallbacks(updateRunnable!!)
        isRunning = false
    }

    fun registerListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun updateProfileData(activity: Context) {
        val context = activity.applicationContext
        val (savedUser, savedPass) = SharedPrefs(context).getCredentials()
        if (savedUser != null && savedPass != null) {
            // Show loading
            if (activity is LoadingOverlayHandler) activity.showLoadingOverlay()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClient.getInfoService.getInfo(savedUser, savedPass)

                    withContext(Dispatchers.Main) {
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()

                        if (response.isSuccessful) {
                            val lastUpdate = System.currentTimeMillis()
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(context).saveInfo(responseText)
                            SharedPrefs(context).saveLastUpdate(lastUpdate)
                            listeners.forEach { it() }
                        } else {
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (activity is LoadingOverlayHandler) activity.hideLoadingOverlay()
                        Toast.makeText(context, "Network error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
