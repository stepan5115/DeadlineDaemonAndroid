package ru.zuevs5115.deadlinedaemon.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.AssignmentAdapter
import ru.zuevs5115.deadlinedaemon.databinding.ActivityAssignmentsBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogCreateAssignmentBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogDeleteAssignmentBinding
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.GetData
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileEditor
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale

class AssignmentsActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding to setUp UI
    private lateinit var binding: ActivityAssignmentsBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout
    //save adapter to update content of RecyclerView
    private lateinit var adapter: AssignmentAdapter
    private var clearSubjects: Subject = Subject(-1, "\uD83D\uDEABНе выбрано")
    private val FORMATER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private var titleFilter: String? = null
    private var descriptionFilter: String? = null
    private var subjectFilter: Subject? = clearSubjects
    private var groupsFilter: MutableList<Group> = mutableListOf()
    private var deadlineFilter: LocalDateTime? = null

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
        menuInflater.inflate(R.menu.appbar_refresh_add_filter, menu)
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
            R.id.action_filter -> {
                GetData.getAllSubjectsIndependenceUser(this, listOf(this::filterDialog))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun filterDialog() {
        val dialogBinding = DialogDeleteAssignmentBinding.inflate(LayoutInflater.from(this))

        // Загрузка данных
        if (SharedPrefs(this).getAllSubjects() == null) {
            //так как без предметов не может быть и заданий
            Toast.makeText(this, "Нету заданий в системе", Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!).toMutableList()
        subjects.add(clearSubjects)
        val selectedGroups: MutableList<Group> = groupsFilter.toMutableList()
        // Настройка Spinner для предметов
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            subjects.map { it.name }
        )
        dialogBinding.spinnerSubject.setAdapter(subjectAdapter)

        // Настройка выбора групп
        dialogBinding.btnSelectGroups.setOnClickListener {
            GetData.getAllGroupsIndependenceUserForAssignmentContext(this, listOf(this::showGroupSelectionDialog), selectedGroups, dialogBinding)
        }

        // Настройка DatePicker для дедлайна
        dialogBinding.etDeadline.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }

        val btnClearFilters: MaterialButton = dialogBinding.btnClearFilters
        btnClearFilters.setOnClickListener {
            deadlineFilter = null
            dialogBinding.etDeadline.text?.clear()
        }

        dialogBinding.etAssignmentTitle.setText(titleFilter ?: "")
        dialogBinding.etAssignmentDescription.setText(descriptionFilter ?: "")
        if (subjectFilter != null) {
            dialogBinding.spinnerSubject.setText(subjectFilter!!.name, false)
        }
        if (deadlineFilter != null) {
            dialogBinding.etDeadline.setText(FORMATER.format(deadlineFilter!!))
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Настройка фильтров")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("применить") { _, _ ->
                val title = dialogBinding.etAssignmentTitle.text.toString()
                val description = dialogBinding.etAssignmentDescription.text.toString()
                val subject = subjects.find { it.name == dialogBinding.spinnerSubject.text.toString() }
                val deadline = TimeFormatter.fromStringSpaceToLocalDateTime(dialogBinding.etDeadline.text.toString())
                GetData.getAllAssignmentsIndependenceUserDeleteContext(this,
                    listOf(this::setFilterSettings),
                    title, description, subject, selectedGroups, deadline)
            }
            .create()
            .show()
    }
    private fun setFilterSettings(title: String?, description: String?, subject: Subject?, selectedGroups: List<Group>,
                                  deadline: LocalDateTime?) {
        titleFilter = title
        descriptionFilter = description
        subjectFilter = subject
        groupsFilter = selectedGroups.toMutableList()
        deadlineFilter = deadline
        updateRecyclerView()
    }
    private fun showGroupSelectionDialog(
        selectedGroups: MutableList<Group>
    ) {
        if (SharedPrefs(this).getAllGroups() == null) {
            Toast.makeText(this, "Нету групп в системе", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!).toList()
            val groupNames = groups.map { it.name }.toTypedArray()
            val checkedItems = groups.map { group ->
                selectedGroups.any { it.id == group.id }
            }.toBooleanArray()

            MaterialAlertDialogBuilder(this)
                .setTitle("Выберите группы")
                .setMultiChoiceItems(groupNames, checkedItems) { _, which, isChecked ->
                    val selectedGroup = groups[which]
                    if (isChecked) {
                        if (!selectedGroups.contains(selectedGroup)) {
                            selectedGroups.add(selectedGroup)
                        }
                    } else {
                        selectedGroups.removeAll { it.id == selectedGroup.id }
                    }
                }
                .setPositiveButton("OK") { _, _ -> }
                .show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Ошибка при формировании списка групп", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showDatePickerDialog(dialogBinding: ViewBinding) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дедлайн")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
            }

            // Показываем TimePicker после выбора даты
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            val timePicker = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(java.util.Calendar.MINUTE, selectedMinute)

                    // Форматируем в строку
                    if (dialogBinding is DialogCreateAssignmentBinding)
                        dialogBinding.etDeadline.setText(FORMATER.format(calendar.time))
                    if (dialogBinding is DialogDeleteAssignmentBinding)
                        dialogBinding.etDeadline.setText(FORMATER.format(calendar.time))
                },
                hour,
                minute,
                true // 24-часовой формат
            )
            timePicker.setTitle("Выберите время дедлайна")
            timePicker.show()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
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
                    //already here
                }
                R.id.nav_settings -> {
                    //go to settings activity and finish itself
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
                R.id.nav_subjects -> {
                    //start activity subject and finish itself
                    startActivity(Intent(this, SubjectsActivity::class.java))
                    finish()
                }
                R.id.nav_groups -> {
                    //start activity groups and finish itself
                    startActivity(Intent(this, GroupsActivity::class.java))
                    finish()
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
                getString(R.string.failed_load_assignments),
                Toast.LENGTH_SHORT
            ).show()
            //set activities
            startActivity(Intent(this, ProfileInfoActivity::class.java))
            finish()
        }
        //getAssignments
        val groupsName: List<String> = groupsFilter.map { it.name }
        var assignments = Parser.fromJsonToAssignments(json!!).toList()
        if ((titleFilter != null) && (titleFilter!!.isNotEmpty()))
            assignments = assignments.filter { it.title.equals(titleFilter, ignoreCase = true) }
        if ((descriptionFilter != null) && (descriptionFilter!!.isNotEmpty()))
            assignments = assignments.filter { it.description.equals(descriptionFilter, ignoreCase = true) }
        if ((subjectFilter != null) && (subjectFilter != clearSubjects))
            assignments = assignments.filter { it.subject == subjectFilter!!.name }
        if (groupsFilter.isNotEmpty())
            assignments = assignments.filter { assignment ->
                assignment.groups.any {
                    groupsName.contains(it)
                }
            }
        if (deadlineFilter != null)
            assignments = assignments.filter { it.deadline.isBefore(deadlineFilter) }
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
    //simply transferring its contents will not work, because parameters are required.
    // That's why this function is needed.
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
        val groupNames = completedAssignments.map { it.title }
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
            .setTitle(getString(R.string.choose_assignment))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                val selectedTitle = autoCompleteTextView.text.toString()
                val selectedAssignment = completedAssignments.find { it.title == selectedTitle }
                if (selectedAssignment != null) {
                    processSelectedAssignment(selectedAssignment)
                } else {
                    Toast.makeText(this, getString(R.string.have_not_choose_assignment), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.No), null)
            .show()
    }
    //process selected from dialog to incomplete assignment
    private fun processSelectedAssignment(assignment: Assignment) {
        ProfileEditor.inCompleteAssignment(assignment.id.toString(), this, listOf(this::tmp))
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