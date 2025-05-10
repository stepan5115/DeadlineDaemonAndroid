package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
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
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityProfileBinding
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import org.json.JSONObject
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter

class ProfileInfoActivity : AppCompatActivity() {
    //binding for edit all elements
    private lateinit var binding: ActivityProfileBinding
    //service for requesting/responding
    private val infoService = ApiClient.getInfoService
    //
    private lateinit var drawerLayout: DrawerLayout

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
            }
            catch (e: Throwable) {
                updateProfileData()
            }
        } else {
            updateProfileData()
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

    private fun loadProfileData(jsonString: String) {
        try {
            //parse json response
            val json = JSONObject(jsonString)

            //base information
            binding.tvUserId.text = json.getInt("user_id").toString()
            binding.tvUsername.text = json.getString("username")
            binding.tvGroups.text = json.getJSONArray("groups").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }
            //settings
            binding.tvCanEditTasks.text = if (json.getBoolean("canEditTasks")) "Да" else "Нет"
            binding.tvAllowNotifications.text = if (json.getBoolean("allowNotifications")) getString(R.string.on_notifications)
                else getString(R.string.off_notifications)
            //convert time in readable format
            val seconds = if (json.has("notificationIntervalSeconds")) json.getInt("notificationIntervalSeconds") else null
            binding.tvNotificationInterval.text = TimeFormatter.formatNotificationInterval(seconds, this)
            //excluded subjects
            binding.tvExcludedSubjects.text = json.getJSONArray("notificationExcludedSubjects").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }
            //complete assignments
            binding.tvCompletedAssignments.text = json.getJSONArray("completedAssignments").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }
            //update username
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvMenuUsername).text =
                json.getString("username")
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
                            loadProfileData(responseText)
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
    }
}