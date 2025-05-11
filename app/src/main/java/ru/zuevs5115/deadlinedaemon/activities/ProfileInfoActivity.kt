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
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ActivityProfileBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.entities.User
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter

class ProfileInfoActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding for edit all elements
    private lateinit var binding: ActivityProfileBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        //base initialization
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set toolbar and save drawerLayout to close/open menu if we need
        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout

        //init burger menu
        supportActionBar?.apply {
            //add "button" to actionBar
            setDisplayHomeAsUpEnabled(true)
            //set image (burger)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        //setup menu
        setupNavigation()
        //to avoid making a new request to the server every time you turn the screen
        loadProfileData()
    }
    //setup navigation menu
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    //already open
                }
                R.id.nav_tasks -> {
                    //start activity assignments and finish itself
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                }
                R.id.nav_settings -> {
                    //will be SUPER COOL CODE
                }
                R.id.nav_refresh -> {
                    //update information (make request)
                    ProfileUpdater.updateProfileData(this, listOf(this::loadProfileData))
                }
                R.id.nav_logout -> {
                    //logout
                    logout()
                }
            }
            //close menu where item selected
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    //setup actions for each button in toolbar
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
                ProfileUpdater.updateProfileData(this, listOf(this::loadProfileData))
                true
            }
            //make what you wand
            else -> super.onOptionsItemSelected(item)
        }
    }
    //load menu xml
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //make menu from xml
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }
    //fill UI information about profile
    private fun loadProfileData() {
        try {
            //if not information
            if (SharedPrefs(this).getInfo() == null) {
                //toast about fail fill UI
                Toast.makeText(
                    this@ProfileInfoActivity,
                    getString(R.string.failed_load),
                    Toast.LENGTH_SHORT
                ).show()
                //try update sharedPrefs for next iterations
                ProfileUpdater.updateProfileData(this, listOf())
                return
            }
            //try parse user information
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
            //update menu username
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvMenuUsername).text =
                user.username
            //update last update
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
    //logout
    private fun logout() {
        //clear login information
        SharedPrefs(this).clearInformation()
        //go to login and clear information
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    //if back button pressed
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //if toolbar "button" pressed then open/close menu
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            //else go to login and clear information
            startActivity(Intent(this, LoginActivity::class.java))
            SharedPrefs(this).clearInformation()
            finish()
        }
    }
    //show progress bar
    override fun showLoadingOverlay() {
        //show process bar
        binding.loadingOverlay.visibility = View.VISIBLE
    }
    //hide progress bar
    override fun hideLoadingOverlay() {
        //hide process bar
        binding.loadingOverlay.visibility = View.GONE
    }
}