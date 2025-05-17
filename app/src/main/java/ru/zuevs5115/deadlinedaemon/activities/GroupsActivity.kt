package ru.zuevs5115.deadlinedaemon.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.GroupAdapter
import ru.zuevs5115.deadlinedaemon.databinding.ActivityGroupsBinding
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileEditor
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class GroupsActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding to setUp UI
    private lateinit var binding: ActivityGroupsBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout
    //save adapter to update content of RecyclerView
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //base init
        super.onCreate(savedInstanceState)
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set content of RecyclerView (add processor for items)
        adapter = GroupAdapter(emptyList()) { group ->
            showMarkAsExitDialog(group)
        }
        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(this@GroupsActivity)
            adapter = this@GroupsActivity.adapter
        }
        //update information before setUp UI
        if (SharedPrefs(this).getInfo() == null)
            ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
        else
            updateRecyclerView()
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
    }
    //set menu xml
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar_refresh_add, menu)
        return true
    }
    //setup actions for toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //burger menu
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            //update information and update RecyclerView
            R.id.action_refresh -> {
                ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
                true
            }
            //add activities
            R.id.action_add -> {
                ProfileUpdater.getAllGroups(this, listOf(this::showEnterGroupDialog))
                true
            }
            //else make what you want
            else -> super.onOptionsItemSelected(item)
        }
    }
    //setup navigation menu
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    //go to profile info activity and finish itself
                    startActivity(Intent(this, ProfileInfoActivity::class.java))
                    finish()
                }
                R.id.nav_tasks -> {
                    //go to tasks activity and finish itself
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                }
                R.id.nav_settings -> {
                    //go to settings and finish itself
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
                R.id.nav_subjects -> {
                    //start activity subject and finish itself
                    startActivity(Intent(this, SubjectsActivity::class.java))
                    finish()
                }
                R.id.nav_groups -> {
                    //already here
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
                R.id.nav_refresh -> {
                    //update information (request to server) and update RecyclerView
                    ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
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
    //logout
    private fun logout() {
        //clear login information
        SharedPrefs(this).clearInformation()
        //go to login
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
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
    }
    //update RecyclerView
    private fun updateRecyclerView() {
        //get last response information
        val json = SharedPrefs(this).getInfo()
        //if null
        if (json == null) {
            //make toast about error
            Toast.makeText(
                this,
                getString(R.string.failed_load_groups),
                Toast.LENGTH_SHORT
            ).show()
            //set activities
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
        //getAssignments
        val groups: Set<Group> = Parser.fromJsonToGroups(json!!)
        //if doesn't have assignments
        if (groups.isEmpty()) {
            //make toast about empty
            Toast.makeText(
                this,
                getString(R.string.empty_groups),
                Toast.LENGTH_SHORT
            ).show()
        }
        //set content of RecyclerView (add processor for items)
        adapter.updateData(groups.toList())
    }
    //function for process items
    private fun showMarkAsExitDialog(group: Group) {
        //make alert dialog
        AlertDialog.Builder(this)
            //setTitle
            .setTitle(getString(R.string.mark_as_exit))
            //setMessage
            .setMessage(getString(R.string.sure_mark_as_exit, group.name))
            //set name of positive button
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                //mark assignment as completed and synchronized with server
                exitGroup(group)
            }
            //set name of negative button
            .setNegativeButton(getString(R.string.No), null)
            //create dialog
            .create()
            //show dialog
            .show()
    }
    //process mark assignment as completed
    private fun exitGroup(group: Group) {
        ProfileEditor.exitGroup(group.name, this, listOf(this::tmp))
    }
    //function to update information if success complete assignment
    //success response -> update information -> update RecyclerView
    //simply transferring its contents will not work, because parameters are required.
    // That's why this function is needed.
    private fun tmp() {
        ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
    }
    //where choose item from list
    private fun showEnterGroupDialog() {
        //get data
        val groupsWithoutUser = Parser.fromJsonToGroups(SharedPrefs(this).getGroups()!!).toList()
        if (groupsWithoutUser.isEmpty()) {
            Toast.makeText(this, getString(R.string.have_no_excluded_groups), Toast.LENGTH_SHORT).show()
            return
        }
        val groupNames = groupsWithoutUser.map { it.name }
        //setup dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_spinner_groups, null)
        val autoCompleteTextView = dialogView.findViewById<AutoCompleteTextView>(R.id.groupDropdown)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, groupNames)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
        //create dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_group))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                val selectedName = autoCompleteTextView.text.toString()
                val selectedGroup = groupsWithoutUser.find { it.name == selectedName }
                if (selectedGroup != null) {
                    processSelectedGroups(selectedGroup)
                } else {
                    Toast.makeText(this, getString(R.string.have_not_choose_group), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.No), null)
            .show()
    }

    private fun processSelectedGroups(group: Group) {
        ProfileEditor.enterGroup(group.name, this, listOf(this::tmp))
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