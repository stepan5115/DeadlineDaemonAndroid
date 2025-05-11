package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import ru.zuevs5115.deadlinedaemon.enities.Assignment
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class AssignmentsActivity : AppCompatActivity(), LoadingOverlayHandler {
    private lateinit var binding: ActivityAssignmentsBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = SharedPrefs(this).getInfo()
        if (json == null) {
            Toast.makeText(
                this,
                getString(R.string.failed_load_assignments),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        val assignments: Set<Assignment> = Parser.fromJsonToAssignments(json!!)
        binding.rvAssignments.apply {
            layoutManager = LinearLayoutManager(this@AssignmentsActivity)
            adapter = AssignmentAdapter(assignments.toList())
        }
        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout
        supportActionBar?.apply {
            //add "button" to actionBar
            setDisplayHomeAsUpEnabled(true)
            //set image (burger)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        setupNavigation()

        Log.d("DEBUG", "Loaded assignments: ${assignments.size}")
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.assignments_menu, menu)  // Правильное меню
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_refresh -> {
                ProfileUpdater.updateProfileData(this)
                true
            }
            R.id.action_add -> {
                Toast.makeText(this, "ADD", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileInfoActivity::class.java))
                    finish()
                }
                R.id.nav_tasks -> {

                }
                R.id.nav_settings -> {
                    // startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_refresh -> {
                    ProfileUpdater.updateProfileData(this)
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
            startActivity(Intent(this, ProfileInfoActivity::class.java))
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
}