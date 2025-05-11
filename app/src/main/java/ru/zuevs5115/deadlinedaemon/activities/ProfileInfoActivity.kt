package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityProfileBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.enities.Assignment
import ru.zuevs5115.deadlinedaemon.enities.User
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration

class ProfileInfoActivity : AppCompatActivity(), LoadingOverlayHandler {
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
    private val assignments: MutableSet<Assignment> = mutableSetOf()
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
        try {
            loadProfileData()
        }
        catch (e: Throwable) {
            ProfileUpdater.updateProfileData(this)
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
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                }
                R.id.nav_settings -> {
                    // startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_refresh -> {
                    ProfileUpdater.updateProfileData(this)
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
                ProfileUpdater.updateProfileData(this)
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
                ProfileUpdater.updateProfileData(this)
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
            binding.tvLastUpdate.text = SharedPrefs(this).getLastUpdate().takeIf { it != 0L }
                ?.let { TimeFormatter.formatTimestamp(it) }
                ?: getString(R.string.not_update)

        } catch (e: Exception) {
            //make toast about parse error
            Toast.makeText(this@ProfileInfoActivity, getString(R.string.failed_load), Toast.LENGTH_SHORT).show()
            //go to login
            startActivity(Intent(this, LoginActivity::class.java))
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
            startActivity(Intent(this, LoginActivity::class.java))
            SharedPrefs(this).clearCredentials()
            finish()
        }
    }
    override fun showLoadingOverlay() {
        //show process bar
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    override fun hideLoadingOverlay() {
        //hide process bar
        binding.loadingOverlay.visibility = View.GONE
    }
    override fun onResume() {
        super.onResume()
        ProfileUpdater.start(this.applicationContext, mutableListOf(this::loadProfileData))
    }

    override fun onPause() {
        super.onPause()
        ProfileUpdater.stop()
    }
}