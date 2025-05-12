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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.SubjectAdapter
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivitySubjectsBinding
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileEditor
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class SubjectsActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding to setUp UI
    private lateinit var binding: ActivitySubjectsBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout
    //save adapter to update content of RecyclerView
    private lateinit var adapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //base init
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set content of RecyclerView (add processor for items)
        adapter = SubjectAdapter(emptyList()) { subject ->
            showMarkAsExcludeDialog(subject)
        }
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(this@SubjectsActivity)
            adapter = this@SubjectsActivity.adapter
        }
        //update information before setUp UI
        if (SharedPrefs(this).getSubjects() == null)
            ProfileUpdater.getAllSubjects(this, listOf(this::updateRecyclerView))
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
                ProfileUpdater
                ProfileUpdater.updateProfileData(this, listOf(this::tmpForUpdateAndGetSubjects))
                true
            }
            //add activities
            R.id.action_add -> {
                showIncludeSubjectDialog()
                true
            }
            //else make what you want
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun tmpForUpdateAndGetSubjects() {
        ProfileUpdater.getAllSubjects(this, listOf(this::updateRecyclerView))
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
                    //go to tasks activity
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    //finish itself
                    finish()
                }
                R.id.nav_settings -> {
                    //go to settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                    //finish itself
                    finish()
                }
                R.id.nav_refresh -> {
                    //update information (request to server) and update RecyclerView
                    ProfileUpdater.updateProfileData(this, listOf(this::tmpForUpdateAndGetSubjects))
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
        val json = SharedPrefs(this).getSubjects()
        //if null
        if (json == null) {
            //make toast about error
            Toast.makeText(
                this,
                getString(R.string.failed_load_subjects),
                Toast.LENGTH_SHORT
            ).show()
            //set activities
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
        //getAssignments
        val subjects: Set<Subject> = Parser.fromJsonToSubjects(json!!)
        //if doesn't have assignments
        if (subjects.isEmpty()) {
            //make toast about empty
            Toast.makeText(
                this,
                getString(R.string.empty_subjects),
                Toast.LENGTH_SHORT
            ).show()
        }
        //set content of RecyclerView (add processor for items)
        adapter.updateData(subjects.toList())
    }
    //function for process items
    private fun showMarkAsExcludeDialog(subject: Subject) {
        //make alert dialog
        AlertDialog.Builder(this)
            //setTitle
            .setTitle(getString(R.string.mark_as_completed))
            //setMessage
            .setMessage(getString(R.string.sure_mark_as_completed, subject.name))
            //set name of positive button
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                //mark assignment as completed and synchronized with server
                markSubjectAsExcluded(subject)
            }
            //set name of negative button
            .setNegativeButton(getString(R.string.No), null)
            //create dialog
            .create()
            //show dialog
            .show()
    }
    //process mark assignment as completed
    private fun markSubjectAsExcluded(subject: Subject) {
        ProfileEditor.excludeSubject(subject.id.toString(), this, listOf(this::tmpForExclude))
    }
    private fun tmpForExclude() {
        ProfileUpdater.updateProfileData(this, listOf(this::tmpForUpdateAndGetSubjects))
    }
    //function to update information if success complete assignment
    //success response -> update information -> update RecyclerView
    private fun tmp() {
        ProfileUpdater.updateProfileData(this, listOf(this::updateRecyclerView))
    }
    //create and show dialog to make assignment incomplete
    private fun showIncludeSubjectDialog() {
        //get complete assignments
        val excludedSubjects = Parser.getExcludeSubjects(SharedPrefs(this).getInfo()!!).toList()
        //if have not complete assignments
        if (excludedSubjects.isEmpty()) {
            //make toast about it
            Toast.makeText(this, getString(R.string.have_no_excluded_subject), Toast.LENGTH_SHORT).show()
            return
        }
        val items = excludedSubjects.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(items.size) { false }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_subjects))
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(getString(R.string.apply)) { _, _ ->
                val selectedItems = mutableListOf<Subject>()
                checkedItems.forEachIndexed { index, isChecked ->
                    if (isChecked) {
                        selectedItems.add(excludedSubjects[index])
                    }
                }
                processSelectedSubjects(selectedItems)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun processSelectedSubjects(subjects: List<Subject>) {
        showLoadingOverlay()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Выполняем запросы последовательно
                subjects.forEach { subject ->
                    val success = withContext(Dispatchers.IO) {
                        try {
                            val (savedUser, savedPass) = SharedPrefs(this@SubjectsActivity).getCredentials()
                            if (savedUser != null && savedPass != null) {
                                val response = ApiClient.includeSubjectService
                                    .includeSubject(savedUser, savedPass, subject.id.toString())
                                response.isSuccessful
                            } else false
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (!success) {
                        // Обработка ошибки для конкретного предмета
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SubjectsActivity,
                                "Ошибка при включении предмета ${subject.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                // Обновляем данные один раз после всех запросов
                withContext(Dispatchers.Main) {
                    hideLoadingOverlay()
                    ProfileUpdater.updateProfileData(
                        this@SubjectsActivity,
                        listOf(this@SubjectsActivity::tmpForUpdateAndGetSubjects)
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingOverlay()
                    Toast.makeText(
                        this@SubjectsActivity,
                        "Ошибка сети",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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