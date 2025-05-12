package ru.zuevs5115.deadlinedaemon.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.AssignmentAdapter
import ru.zuevs5115.deadlinedaemon.databinding.ActivityAssignmentsBinding
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileEditor
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class AssignmentsActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding to setUp UI
    private lateinit var binding: ActivityAssignmentsBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout
    //save adapter to update content of RecyclerView
    private lateinit var adapter: AssignmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //base init
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set content of RecyclerView (add processor for items)
        adapter = AssignmentAdapter(emptyList()) { assignment ->
            showMarkAsCompleteDialog(assignment)
        }
        binding.rvAssignments.apply {
            layoutManager = LinearLayoutManager(this@AssignmentsActivity)
            adapter = this@AssignmentsActivity.adapter
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
        menuInflater.inflate(R.menu.assignments_menu, menu)
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
                showCompletedAssignmentsDialog()
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
                    //go to profile info activity
                    startActivity(Intent(this, ProfileInfoActivity::class.java))
                    //finish itself
                    finish()
                }
                R.id.nav_tasks -> {
                    //already here
                }
                R.id.nav_settings -> {
                    //will be SUPER COOL CODE
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
        //finish
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
                getString(R.string.failed_load_assignments),
                Toast.LENGTH_SHORT
            ).show()
            //set activities
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
        //getAssignments
        val assignments: Set<Assignment> = Parser.fromJsonToAssignments(json!!)
        //if doesn't have assignments
        if (assignments.isEmpty()) {
            //make toast about empty
            Toast.makeText(
                this,
                getString(R.string.empty_assignments),
                Toast.LENGTH_SHORT
            ).show()
        }
        //set content of RecyclerView (add processor for items)
        adapter.updateData(assignments.toList())
    }
    //function for process items
    private fun showMarkAsCompleteDialog(assignment: Assignment) {
        //make alert dialog
        AlertDialog.Builder(this)
            //setTitle
            .setTitle(getString(R.string.mark_as_completed))
            //setMessage
            .setMessage(getString(R.string.sure_mark_as_completed, assignment.title))
            //set name of positive button
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                //mark assignment as completed and synchronized with server
                markAssignmentAsComplete(assignment)
            }
            //set name of negative button
            .setNegativeButton(getString(R.string.No), null)
            //create dialog
            .create()
            //show dialog
            .show()
    }
    //process mark assignment as completed
    private fun markAssignmentAsComplete(assignment: Assignment) {
        ProfileEditor.completeAssignment(assignment.id.toString(), this, listOf(this::tmp))
    }
    //function to update information if success complete assignment
    //success response -> update information -> update RecyclerView
    private fun tmp() {
        ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
    }
    //create and show dialog to make assignment incomplete
    private fun showCompletedAssignmentsDialog() {
        //get complete assignments
        val completedAssignments = Parser.getCompletedAssignments(SharedPrefs(this).getInfo()!!).toList()
        //if have not complete assignments
        if (completedAssignments.isEmpty()) {
            //make toast about it
            Toast.makeText(this, getString(R.string.have_no_completed_assignments), Toast.LENGTH_SHORT).show()
            return
        }
        val items = completedAssignments.map { it.title }.toTypedArray()
        val checkedItems = BooleanArray(items.size) { false }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_assignments))
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(getString(R.string.apply)) { _, _ ->
                val selectedItems = mutableListOf<Assignment>()
                checkedItems.forEachIndexed { index, isChecked ->
                    if (isChecked) {
                        selectedItems.add(completedAssignments[index])
                    }
                }
                processSelectedAssignments(selectedItems)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun processSelectedAssignments(assignments: List<Assignment>) {
        assignments.forEach { assignment ->
            ProfileEditor.inCompleteAssignment(assignment.id.toString(), this, listOf(this::tmp))
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