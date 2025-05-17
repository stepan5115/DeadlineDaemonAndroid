package ru.zuevs5115.deadlinedaemon.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ActivitySettingsBinding
import ru.zuevs5115.deadlinedaemon.entities.User
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileEditor
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter

class SettingsActivity : AppCompatActivity(), LoadingOverlayHandler {
    //static TIME_UNIT_MULTIPLIERS to work with interval
    companion object {
        private val TIME_UNIT_MULTIPLIERS = longArrayOf(1L, 60L, 3600L, 86400L)
    }
    //base vars
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var drawerLayout : DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        //base initialization
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
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
        //setup buttons listeners
        setupListeners()
        //loading initial data
        loadInitialData()
    }
    //setup navigation menu
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //set to profile information
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileInfoActivity::class.java))
                    finish()
                }
                //set to assignments
                R.id.nav_tasks -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                }
                R.id.nav_settings -> {
                    //already there
                }
                //set to subjects
                R.id.nav_subjects -> {
                    startActivity(Intent(this, SubjectsActivity::class.java))
                    finish()
                }
                //set to groups
                R.id.nav_groups -> {
                    startActivity(Intent(this, GroupsActivity::class.java))
                    finish()
                }
                R.id.nav_management -> {
                    //start activity management if have rights or toast  about haven't enough rights
                    val tmp: String? = SharedPrefs(this).getInfo()
                    if (tmp == null)
                        Toast.makeText(
                            this,
                            getString(R.string.can_not_find_user),
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        val user = Parser.fromJsonToUser(tmp)
                        if (user.canEditTasks) {
                            startActivity(Intent(this, ManagementActivity::class.java))
                            finish()
                        } else
                            Toast.makeText(
                                this,
                                getString(R.string.you_have_not_rights),
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
                //update information (make request)
                R.id.nav_refresh -> {
                    ProfileUpdater.updateProfileData(this, listOf(this::fillDefaultValues))
                }
                //logout
                R.id.nav_logout -> {
                    logout()
                }
            }
            //close menu where item selected
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    //logout
    private fun logout() {
        //clear login information
        SharedPrefs(this).clearInformation()
        //go to login and finish
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    //setup menu toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //make menu from xml
        menuInflater.inflate(R.menu.appbar_refresh, menu)
        return true
    }
    //setup toolbar actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //open/close menu
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            //refresh data
            R.id.action_refresh -> {
                ProfileUpdater.updateProfileData(this, listOf(this::fillDefaultValues))
                true
            }
            //do what you want
            else -> super.onOptionsItemSelected(item)
        }
    }
    //if back button pressed
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //if toolbar "button" pressed then open/close menu
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            //else go to login and clear information
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
    }
    //setup buttons listeners
    private fun setupListeners() {
        //setup allow notification change
        binding.switchAllowNotifications.setOnCheckedChangeListener { _, isChecked ->
            updateAllowNotifications(isChecked)
        }
        //setup interval button
        binding.buttonIntervalPicker.setOnClickListener {
            showIntervalPickerDialog()
        }
        //setup edit permission button
        binding.buttonEditPermission.setOnClickListener {
            checkEditPermissions()
        }
    }
    //load initial data
    private fun loadInitialData() {
        //if have information
        if (SharedPrefs(this).getInfo() == null) {
            //try update (request) and fillDefaultValues
            ProfileUpdater.updateProfileData(this, listOf(this::fillDefaultValues))
        } else {
            //fill defaultValues
            fillDefaultValues()
        }
    }
    //fill default values
    private fun fillDefaultValues() {
        try {
            //if haven't information
            if (SharedPrefs(this).getInfo() == null) {
                //toast about fail fill UI
                Toast.makeText(
                    this,
                    getString(R.string.failed_load),
                    Toast.LENGTH_SHORT
                ).show()
                //try update sharedPrefs for next iterations
                ProfileUpdater.updateProfileData(this, listOf())
                //we can't set settings without information => go to ProfileInfoActivity and finish itself
                startActivity(Intent(this, ProfileInfoActivity::class.java))
                finish()
            }
            //get user from json information
            val user: User = Parser.fromJsonToUser(SharedPrefs(this).getInfo()!!)
            //off listener (if don't do that, trigger updateAllowNotifications
            binding.switchAllowNotifications.setOnCheckedChangeListener(null)
            //bind allow notifications value
            binding.switchAllowNotifications.isChecked = user.allowNotifications
            //on listener
            binding.switchAllowNotifications.setOnCheckedChangeListener { _, isChecked ->
                updateAllowNotifications(isChecked)
            }
            //get seconds interval
            val interval = user.notificationIntervalSeconds
            //update interval in UI
            updateIntervalDisplay(interval)
        } catch (e: Exception) {
            //make toast about parse error
            Toast.makeText(this, getString(R.string.failed_load), Toast.LENGTH_SHORT).show()
            //go to login and finish itself
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    //update interval in UI
    private fun updateIntervalDisplay(seconds: Long) {
        //if interval not specified
        if (seconds <= 0) {
            binding.textIntervalDisplay.text = getString(R.string.not_specified)
            return
        }
        //process second to string and fill UI
        binding.textIntervalDisplay.text = TimeFormatter.formatNotificationInterval(seconds, this)
    }
    //make interval dialog
    private fun showIntervalPickerDialog() {
        //make dialog from xml
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_interval_picker, null)
        //setup UI
        val editValue = dialogView.findViewById<EditText>(R.id.edit_interval_value)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinner_interval_unit)
        //get information about selected interval to fill default values in dialog
        var displayValue = 0L
        var selectedUnitIndex = 0
        try {
            val result = TimeFormatter.extractLargestTimeUnit(
                Parser.fromJsonToUser(SharedPrefs(this).getInfo()!!).notificationIntervalSeconds, this)
            displayValue = result.first
            selectedUnitIndex = result.second
        } catch (_: Exception) { }
        //fill default values in dialog
        editValue.setText(displayValue.toString())
        //create adapter to choose measure
        ArrayAdapter(this, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.time_units)).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUnit.adapter = adapter
            //setup default measure
            spinnerUnit.setSelection(selectedUnitIndex)
        }
        //make dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.interval_picker_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.OK)) { _, _ ->
                //get count
                val value = editValue.text.toString().toLongOrNull() ?: 0L
                //get measure
                val unitIndex = spinnerUnit.selectedItemPosition
                //convert to second
                val seconds = value * TIME_UNIT_MULTIPLIERS[unitIndex]
                //make main action
                updateNotificationInterval(seconds)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //make token dialog
    private fun showTokenInputDialog() {
        //setup from xml
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_token_input, null)
        //add editText for token
        val editToken = dialogView.findViewById<EditText>(R.id.edit_token)
        //make dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.token_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.token_dialog_confirm)) { _, _ ->
                //get token form editText
                val token = editToken.text.toString()
                //if have token
                if (token.isNotBlank()) {
                    //start main action
                    onEditPermissionTokenEntered(token)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //main acton for set edit permission
    private fun onEditPermissionTokenEntered(token: String) {
        ProfileEditor.getAdminRights(token, this, listOf(this::tmp))
    }
    //check permission
    private fun checkEditPermissions() {
        //try get user and check edit permission status
        val user = try {
            Parser.fromJsonToUser(SharedPrefs(this).getInfo()!!)
        } catch (e: Exception) {
            null
        }
        //if already have, don't show dialog
        if (user?.canEditTasks == true) {
            Toast.makeText(this, getString(R.string.edit_permission_message), Toast.LENGTH_SHORT).show()
            return
        }
        //else show dialog
        showTokenInputDialog()
    }
    private fun updateAllowNotifications(value: Boolean) {
        ProfileEditor.setNotificationStatus(value, this, listOf(this::tmp))
    }
    //function to update information if success complete assignment
    //success response -> update information -> update settings UI
    private fun tmp() {
        ProfileUpdater.updateProfileData(this, listOf(this::fillDefaultValues))
    }

    private fun updateNotificationInterval(seconds: Long) {
        ProfileEditor.setIntervalService(seconds, this, listOf(this::tmp))
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