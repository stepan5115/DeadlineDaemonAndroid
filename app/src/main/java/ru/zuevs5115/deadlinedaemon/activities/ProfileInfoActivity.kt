package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityProfileBinding
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import org.json.JSONObject
import ru.zuevs5115.deadlinedaemon.enities.Assignment
import ru.zuevs5115.deadlinedaemon.enities.User
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration

class ProfileInfoActivity : AppCompatActivity() {
    //binding for edit all elements
    private lateinit var binding: ActivityProfileBinding
    //service for requesting/responding
    private val infoService = ApiClient.getInfoService
    private lateinit var drawerLayout: DrawerLayout
    //auto-updating interval ~ 5 minutes in milliseconds
    private val updateInterval = 5 * 60 * 1000L
    //for auto-updating
    private lateinit var updateHandler: Handler
    private lateinit var updateRunnable: Runnable
    //store assignments and interval for another activities
    private val assignmentsMap = mutableMapOf<Long, Assignment>()
    private var interval : Duration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set toolbar and save drawerLayout to close/open if we need
        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout

        supportActionBar?.apply {
            //add "button" to actionBar
            setDisplayHomeAsUpEnabled(true)
            //set image (burger)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        setupNavigation()
        //to avoid making a new request to the server every time you turn the screen
        if (savedInstanceState != null) {
            try {
                binding.tvUserId.text = savedInstanceState.getString("id")
                binding.tvUsername.text = savedInstanceState.getString("name")
                binding.tvGroups.text = savedInstanceState.getString("groups")
                binding.tvCanEditTasks.text = savedInstanceState.getString("canEdit")
                binding.tvAllowNotifications.text = savedInstanceState.getString("notifications")
                binding.tvNotificationInterval.text = savedInstanceState.getString("interval")
                binding.tvExcludedSubjects.text = savedInstanceState.getString("excludedSubjects")
                binding.tvCompletedAssignments.text = savedInstanceState.getString("completeAssignments")
                binding.tvLastUpdate.text = savedInstanceState.getString("lastUpdate") ?: getString(R.string.not_update)
                savedInstanceState.getStringArrayList("assignments_data")?.forEach { entry ->
                    val (id, time) = entry.split("|")
                    assignmentsMap[id.toLong()]?.lastNotificationTime = time.toLong()
                }
            }
            catch (e: Throwable) {
                updateProfileData()
                loadProfileData()
            }
        } else {
            updateProfileData()
            loadProfileData()
        }
    }
    //setup navigation menu
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {

                }
                R.id.nav_tasks -> {
                    // startActivity(Intent(this, TasksActivity::class.java))
                }
                R.id.nav_settings -> {
                    // startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_refresh -> {
                    updateProfileData()
                    loadProfileData()
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            //close menu where item selected
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //set toolbar actions
        return when (item.itemId) {
            android.R.id.home -> {
                //open menu
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_refresh -> {
                //refresh data
                updateProfileData()
                loadProfileData()
                true
            }
            //make what you wand
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //make menu from xml
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    private fun loadProfileData() {
        try {
            if (SharedPrefs(this).getInfo() == null) {
                Toast.makeText(
                    this@ProfileInfoActivity,
                    getString(R.string.failed_load),
                    Toast.LENGTH_SHORT
                ).show()
                updateProfileData()
                return
            }
            val user: User = Parser.fromJsonToUser(SharedPrefs(this).getInfo()!!)
            //base information
            binding.tvUserId.text = user.id.toString()
            binding.tvUsername.text = user.username
            binding.tvGroups.text = user.groups
            //settings
            binding.tvCanEditTasks.text = if (user.canEditTasks) getString(R.string.Yes) else
                getString(R.string.No)
            binding.tvAllowNotifications.text = if (user.allowNotifications) getString(R.string.on_notifications)
                else getString(R.string.off_notifications)
            //convert time in readable format
            val seconds = user.notificationIntervalSeconds
            binding.tvNotificationInterval.text = TimeFormatter.formatNotificationInterval(seconds)
            //excluded subjects
            binding.tvExcludedSubjects.text = user.notificationExcludedSubjects
            //complete assignments
            binding.tvCompletedAssignments.text = user.completedAssignments
            //update username
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvMenuUsername).text =
                user.username
        } catch (e: Exception) {
            //make toast about parse error
            Toast.makeText(this@ProfileInfoActivity, getString(R.string.failed_load), Toast.LENGTH_SHORT).show()
            //go to login
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    private fun updateProfileData() {
        //get information from sharedPreferences
        val (savedUser, savedPass) = SharedPrefs(this).getCredentials()
        if (savedUser != null && savedPass != null) {
            //show process bar
            showLoadingOverlay()
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //try get response
                    val response = infoService.getInfo(savedUser, savedPass)
                    //set coroutine thread to Android Main for edit UI (only main can do that)
                    withContext(Dispatchers.Main) {
                        //hide process bar
                        hideLoadingOverlay()
                        if (response.isSuccessful) {
                            //Update last update information
                            val lastUpdate = System.currentTimeMillis()
                            val formattedTime = TimeFormatter.formatTimestamp(lastUpdate)
                            binding.tvLastUpdate.text = getString(R.string.last_update, formattedTime)
                            //parse response
                            val responseText = response.body()?.message ?: ""
                            SharedPrefs(this@ProfileInfoActivity).saveInfo(responseText)
                        } else {
                            val errorMessage = ErrorHandler.handleError(response)
                            //toast about error
                            Toast.makeText(this@ProfileInfoActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            //go to login
                            startActivity(Intent(this@ProfileInfoActivity, LoginActivity::class.java))
                        }
                    }
                } catch (e: Exception) {
                    //set coroutine thread to Android Main
                    withContext(Dispatchers.Main) {
                        //hide process bar
                        hideLoadingOverlay()
                        //go to login
                        Toast.makeText(this@ProfileInfoActivity, "Network error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun logout() {
        //clear login information
        SharedPrefs(this).clearCredentials()
        //go to login
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //if toolbar "button" pressed then open/close menu
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun showLoadingOverlay() {
        //show process bar
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        //hide process bar
        binding.loadingOverlay.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //save data, to avoid making a new request to the server every time you turn the screen
        super.onSaveInstanceState(outState)
        outState.putString("id", binding.tvUserId.text.toString())
        outState.putString("name", binding.tvUsername.text.toString())
        outState.putString("groups", binding.tvGroups.text.toString())
        outState.putString("canEdit", binding.tvCanEditTasks.text.toString())
        outState.putString("notifications", binding.tvAllowNotifications.text.toString())
        outState.putString("interval", binding.tvNotificationInterval.text.toString())
        outState.putString("excludedSubjects", binding.tvExcludedSubjects.text.toString())
        outState.putString("completeAssignments", binding.tvCompletedAssignments.text.toString())
        outState.putString("lastUpdate", binding.tvLastUpdate.text.toString())
        val assignmentsList = assignmentsMap.values.map { assignment ->
            "${assignment.id}|${assignment.lastNotificationTime}"
        }
        outState.putStringArrayList("assignments_data", ArrayList(assignmentsList))
    }
    //check last update interval
    private fun shouldUpdate(): Boolean {
        val lastUpdateText = binding.tvLastUpdate.text.toString()
        return if (lastUpdateText.contains(getString(R.string.not_update))) {
            true
        } else {
            try {
                val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                val lastUpdateTime = format.parse(lastUpdateText.substringAfter(": "))?.time ?: 0
                System.currentTimeMillis() - lastUpdateTime >= updateInterval
            } catch (e: Exception) {
                true
            }
        }
    }
    private fun startAutoUpdate() {
        updateHandler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                if (shouldUpdate()) {
                    updateProfileData()
                    loadProfileData()
                    Toast.makeText(
                        this@ProfileInfoActivity,
                        getString(R.string.auto_update_notification),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //it is possible that when the user wakes up, he has updated the information on
                // his own, then the next time it is triggered, more than 5 minutes may have
                // passed since the last update, but this is not critical.
                updateHandler.postDelayed(this, updateInterval)
            }
        }
        updateHandler.post(updateRunnable)
    }
    private fun stopAutoUpdate() {
        if (::updateHandler.isInitialized) {
            updateHandler.removeCallbacks(updateRunnable)
        }
    }
    override fun onResume() {
        super.onResume()
        startAutoUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopAutoUpdate()
    }
    private fun parseAssignments(assignmentsJson: JSONArray): String {
        val newAssignments = mutableListOf<Assignment>()
        val currentIds = mutableSetOf<Long>()
        val result : StringBuilder = StringBuilder()

        for (i in 0 until assignmentsJson.length()) {
            try {
                val assignmentJson = assignmentsJson.getJSONObject(i)
                val id = assignmentJson.getLong("assignment_id")
                currentIds.add(id)

                val existingAssignment = assignmentsMap[id]
                if (existingAssignment != null) {
                    result.append("\uD83D\uDE80").append(getString(R.string.title_filed)).append(existingAssignment.title).append("\n").
                            append("\uD83D\uDD25").append(getString(R.string.deadline_field)).append(existingAssignment.deadline).append("\n").
                            append("\n")
                } else {
                    result.append("\uD83D\uDE80").append(getString(R.string.title_filed)).append(assignmentJson.getString("title")).append("\n").
                            append("\uD83D\uDD25").append(getString(R.string.deadline_field)).append(assignmentJson.getString("description")).append("\n").
                            append("\n")
                    newAssignments.add(Assignment(
                        id = id,
                        title = assignmentJson.getString("title"),
                        description = assignmentJson.getString("description"),
                        groups = assignmentJson.getJSONArray("groups").let { array ->
                            (0 until array.length()).mapTo(mutableSetOf()) { array.getString(it) }
                        },
                        deadline = assignmentJson.getString("deadline"),
                        subjectId = assignmentJson.getLong("subject_id"),
                        lastNotificationTime = 0
                    ).also { assignmentsMap[id] = it })
                }
            } catch (e: Exception) {
                Log.e("Assignment", "Error parsing assignment", e)
            }
        }

        // Удаляем задания, которых больше нет
        assignmentsMap.keys.removeAll { it !in currentIds }

        return result.toString()
    }
}