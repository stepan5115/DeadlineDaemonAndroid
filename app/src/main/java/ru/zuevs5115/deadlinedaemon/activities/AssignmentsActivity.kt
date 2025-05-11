package ru.zuevs5115.deadlinedaemon.activities

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
        //update information before setUp UI
        ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
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
                Toast.makeText(this, "ADD", Toast.LENGTH_SHORT).show()
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
            //finish activity
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
        //set content of RecyclerView
        adapter = AssignmentAdapter(assignments.toList())
        binding.rvAssignments.apply {
            layoutManager = LinearLayoutManager(this@AssignmentsActivity)
            adapter = this@AssignmentsActivity.adapter
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