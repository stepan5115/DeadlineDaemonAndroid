package ru.zuevs5115.deadlinedaemon.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.TokenAdapter
import ru.zuevs5115.deadlinedaemon.databinding.ActivityManagementBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogAssignmentBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogSimpleInputBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogTokenBinding
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.EditData
import ru.zuevs5115.deadlinedaemon.utils.GetData
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.ProfileUpdater
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class ManagementActivity : AppCompatActivity(), LoadingOverlayHandler {
    //create binding for set up
    private lateinit var binding: ActivityManagementBinding
    //remember to open/close menu
    private lateinit var drawerLayout: DrawerLayout
    //format for work with date
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    //subject that mean no filter by subject
    private lateinit var clearSubjects: Subject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setUp clear subject
        clearSubjects = Subject(-1, getString(R.string.not_selected))
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
        //set up buttons
        setupButtons()
    }
    //load menu xml
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //make menu from xml
        menuInflater.inflate(R.menu.appbar_refresh, menu)
        return true
    }
    //setup navigation menu
    private fun setupNavigation() {
        //set actions for all menu items
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    //start profile info activity and finish itself
                    startActivity(Intent(this, ProfileInfoActivity::class.java))
                    finish()
                }
                R.id.nav_tasks -> {
                    //start activity assignments and finish itself
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                }
                R.id.nav_management -> {
                    //already there
                }
                R.id.nav_settings -> {
                    //start activity settings and finish itself
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
                    //update information (make request)
                    ProfileUpdater.updateProfileData(this, listOf())
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
                ProfileUpdater.updateProfileData(this, listOf())
                true
            }
            //make what you want
            else -> super.onOptionsItemSelected(item)
        }
    }
    //set up buttons
    private fun setupButtons() {
        //setUp buttons
        binding.buttonCreateAssignment.setOnClickListener { GetData.getAllSubjectsIndependenceUser(this,
            listOf(this::showCreateAssignmentDialog)) }
        binding.buttonDeleteAssignment.setOnClickListener { GetData.getAllSubjectsIndependenceUser(this,
            listOf(this::showDeleteAssignmentFilterDialog)) }
        binding.buttonGenerateToken.setOnClickListener { generateToken() }
        binding.buttonCreateSubject.setOnClickListener { showCreateSubjectDialog() }
        binding.buttonDeleteSubject.setOnClickListener { GetData.getAllSubjectsIndependenceUser(this, listOf(this::showDeleteSubjectDialog)) }
        binding.buttonCreateGroup.setOnClickListener { showCreateGroupDialog() }
        binding.buttonDeleteGroup.setOnClickListener { GetData.getAllGroupsIndependenceUserFor(this, listOf(this::showDeleteGroupDialog)) }
        binding.buttonGetTokens.setOnClickListener { GetData.getTokens(this, listOf(this::showGetTokensDialog)) }
    }
    //delete assignments dialog
    private fun showDeleteAssignmentFilterDialog() {
        //get dialog with assignment fields
        val dialogBinding = DialogAssignmentBinding.inflate(LayoutInflater.from(this))
        //try get subjects for choose and toast if have not subjects
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, getString(R.string.no_subjects_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!).toMutableList()
        //add subject that mean no choice
        subjects.add(clearSubjects)
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            subjects.map { it.name }
        )
        dialogBinding.spinnerSubject.setAdapter(subjectAdapter)
        //set subject filter on no choice
        dialogBinding.spinnerSubject.setText(clearSubjects.name, false)
        //set up clear filter button (specially for delete assignments)
        val btnClearFilters: MaterialButton = dialogBinding.btnClearFilters
        btnClearFilters.setOnClickListener {
            dialogBinding.etDeadline.text?.clear()
        }
        //set up group selection
        val selectedGroups: MutableList<Group> = mutableListOf()
        dialogBinding.btnSelectGroups.setOnClickListener {
            GetData.getAllGroupsIndependenceUserForAssignmentContext(this, listOf(this::showGroupSelectionDialog), selectedGroups, dialogBinding)
        }
        //set up choose deadline
        dialogBinding.etDeadline.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }
        //create final dialog
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.filters_settings))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.apply)) { _, _ ->
                val title = dialogBinding.etAssignmentTitle.text.toString()
                val description = dialogBinding.etAssignmentDescription.text.toString()
                var subject = subjects.find { it.name == dialogBinding.spinnerSubject.text.toString() }
                if (subject == clearSubjects)
                    subject = null
                val deadline = TimeFormatter.fromStringSpaceToLocalDateTime(dialogBinding.etDeadline.text.toString())
                //set assignments list accord filters
                GetData.getAllAssignmentsIndependenceUserDeleteContext(this,
                    listOf(this::showDeleteAssignmentDialog),
                    title, description, subject, selectedGroups, deadline)
            }
            .create()
            .show()
    }
    //get date and time picker dialog
    private fun showDatePickerDialog(dialogBinding: ViewBinding) {
        //date picker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.choose_deadline))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
            }
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            //time picker
            val timePicker = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(java.util.Calendar.MINUTE, selectedMinute)
                    //set filter
                    if (dialogBinding is DialogAssignmentBinding)
                        dialogBinding.etDeadline.setText(format.format(calendar.time))
                },
                hour,
                minute,
                true //24-hour format
            )
            timePicker.setTitle(getString(R.string.choose_deadline_time))
            timePicker.show()
        }
        //show final dialog
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }
    //show select group dialog
    private fun showGroupSelectionDialog(
        selectedGroups: MutableList<Group>
    ) {
        //try get groups information
        if (SharedPrefs(this).getAllGroups() == null) {
            Toast.makeText(this, getString(R.string.no_groups_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!).toList()
            val groupNames = groups.map { it.name }.toTypedArray()
            val checkedItems = groups.map { group ->
                selectedGroups.any { it.id == group.id }
            }.toBooleanArray()

            //create dialog
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.choose_groups_filter))
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
                .setPositiveButton(getString(R.string.apply)) { _, _ -> }
                .show()
        } catch (e: Throwable) {
            Toast.makeText(this, getString(R.string.error_while_form_list), Toast.LENGTH_SHORT).show()
        }
    }
    //show a dialog with filtered assignments
    private fun showDeleteAssignmentDialog(title: String?, description: String?, subject: Subject?, groups: List<Group>,
                                           deadline: LocalDateTime?) {
        //try get all system assignments
        if (SharedPrefs(this).getAllAssignments() == null) {
            Toast.makeText(this, getString(R.string.no_assignments_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        //try apply filter
        try {
            val groupsName = groups.map { it.name }
            var assignments = Parser.fromJsonToAssignments(SharedPrefs(this).getAllAssignments()!!).toList()
            if ((title != null) && (title.trim().isNotEmpty()))
                assignments = assignments.filter { it.title == title }
            if ((description != null) && (description.trim().isNotEmpty()))
                assignments = assignments.filter { it.description.equals(description, ignoreCase = true) }
            if (subject != null)
                assignments = assignments.filter { it.subject == subject.name }
            if (groups.isNotEmpty())
                assignments = assignments.filter { assignmentGroups -> assignmentGroups.groups.any { groupsName.contains(it) } }
            if (deadline != null)
                assignments = assignments.filter { it.deadline.isBefore(deadline) }
            if (assignments.isEmpty()) {
                Toast.makeText(this, getString(R.string.have_no_filtering_assignments), Toast.LENGTH_SHORT).show()
                return
            }
            val assignmentTitles = assignments.map { it.title }.toTypedArray()
            //show dialog for deleting
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.deleting_assignments))
                .setItems(assignmentTitles) { _, which ->
                    deleteAssignment(assignments[which].id.toString())
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        catch (e: Throwable) {
            Toast.makeText(this,  getString(R.string.error_forming_assignments), Toast.LENGTH_SHORT).show()
        }
    }
    //show create assignment dialog
    private fun showCreateAssignmentDialog() {
        //get dialog for assignment fields
        val dialogBinding = DialogAssignmentBinding.inflate(LayoutInflater.from(this))
        //try load subjects
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, getString(R.string.no_subjects_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!)
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            subjects.map { it.name }
        )
        dialogBinding.spinnerSubject.setAdapter(subjectAdapter)
        //hide clear date button (no need)
        dialogBinding.btnClearFilters.visibility = View.GONE
        //set up groups part
        val selectedGroups: MutableList<Group> = mutableListOf()
        dialogBinding.btnSelectGroups.setOnClickListener {
            GetData.getAllGroupsIndependenceUserForAssignmentContext(this, listOf(this::showGroupSelectionDialog), selectedGroups, dialogBinding)
        }
        //set up deadline part
        dialogBinding.etDeadline.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }
        //create and set up dialog for create assignment
        //instead of configuring the setPositiveButton button, we make a
        // custom button so that if the validation fails, the dialog does not close.
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.create_assignment))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = dialogBinding.etAssignmentTitle.text.toString()
                val description = dialogBinding.etAssignmentDescription.text.toString()
                val subject = subjects.find { it.name == dialogBinding.spinnerSubject.text.toString() }
                val deadline = dialogBinding.etDeadline.text.toString()
                val groupNames = selectedGroups.map { it.name }
                if (validateAssignmentInput(title, description, subject, deadline, selectedGroups, dialogBinding)) {
                    createAssignment(title, description, groupNames, deadline, subject!!)
                    dialog.dismiss()
                }
            }
        }
        //add the "Create" button after creation so that it is available in setOnShowListener
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.create)) { _, _ -> /* catch */ }
        dialog.show()
    }
    //dialog to get tokens
    private fun showGetTokensDialog() {
        val rawTokens = SharedPrefs(this).getTokens()
        if (rawTokens == null) {
            Toast.makeText(this, getString(R.string.not_find_tokens), Toast.LENGTH_SHORT).show()
            return
        }
        val tokens = Parser.getAdminTokens(rawTokens).toMutableList()
        if (tokens.isEmpty()) {
            Toast.makeText(this, getString(R.string.you_have_no_tokens), Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = TokenAdapter(tokens) { token ->
            //set up copy to buffer
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("token", token.token)
            clipboard.setPrimaryClip(clip)
            //toast about copy
            Toast.makeText(this, getString(R.string.copy_token, token.token), Toast.LENGTH_SHORT).show()
        }

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ManagementActivity)
            this.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.your_tokens))
            .setView(recyclerView)
            .setNegativeButton(getString(R.string.close), null)
            .create()

        adapter.setOnItemLongClickListener { token ->
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.sure_delete_token))
                .setMessage(getString(R.string.absolutely_sure_delete_token, token.token))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    tokens.remove(token)
                    adapter.updateData(tokens)
                    EditData.deleteToken(this, listOf {
                        Toast.makeText(this, getString(R.string.deleted_token, token.token), Toast.LENGTH_SHORT).show()
                    }, token.id.toString())
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
            true
        }

        dialog.show()
    }
    //show create subject dialog
    private fun showCreateSubjectDialog() {
        val dialogBinding = DialogSimpleInputBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etInput.hint = getString(R.string.enter_subject_name)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.create_subject))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = dialogBinding.etInput.text.toString()
                createSubject(name)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //show delete subject dialog
    private fun showDeleteSubjectDialog() {
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, getString(R.string.no_subjects_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!).toList()
        val subjectNames = subjects.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.subject_delete))
            .setItems(subjectNames) { _, which ->
                deleteSubject(subjects[which].id.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //show create group dialog
    private fun showCreateGroupDialog() {
        val dialogBinding = DialogSimpleInputBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etInput.hint = getString(R.string.enter_group_name)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.group_creation))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = dialogBinding.etInput.text.toString()
                createGroup(name)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //show delete group dialog
    private fun showDeleteGroupDialog() {
        if (SharedPrefs(this).getAllGroups() == null) {
            Toast.makeText(this, getString(R.string.no_groups_in_system), Toast.LENGTH_SHORT).show()
            return
        }
        val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!).toList()
        val groupNames = groups.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.group_deletion))
            .setItems(groupNames) { _, which ->
                deleteGroup(groups[which].id.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    //show token dialog after generate
    private fun showTokenDialog(token: String) {
        val dialogBinding = DialogTokenBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvToken.text = token

        dialogBinding.tvToken.setOnClickListener {
            //set up copy buffer
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Token", token)
            clipboard.setPrimaryClip(clip)
            //toast about it
            Toast.makeText(this, getString(R.string.copy_token_simple), Toast.LENGTH_SHORT).show()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.token_created))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.OK), null)
            .show()
    }
    //generate token request
    private fun generateToken() {
        EditData.generateTokenService(this, listOf(this::showTokenDialog))
    }
    //create assignment request
    private fun createAssignment(title: String, description: String, groupNames: List<String>, deadline: String, subject: Subject) {
        EditData.createAssignmentService(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, title, description, Parser.groupNamesToJson(groupNames), deadline, subject.id.toString())
    }
    //delete assignment request
    private fun deleteAssignment(assignmentId: String) {
        EditData.deleteAssignmentService(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, assignmentId)
    }
    //create subject request
    private fun createSubject(name: String) {
        EditData.createSubject(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, name)
    }
    //create subject request
    private fun deleteSubject(subjectId: String) {
        EditData.deleteSubject(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, subjectId)
    }
    //create group request
    private fun createGroup(name: String) {
        EditData.createGroup(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, name)
    }
    //delete group request
    private fun deleteGroup(groupId: String) {
        EditData.deleteGroup(this, listOf {
            Toast.makeText(this, getString(R.string.OK), Toast.LENGTH_SHORT).show()
        }, groupId)
    }
    //check valid fields for creation dialog
    private fun validateAssignmentInput(
        title: String,
        description: String,
        subject: Subject?,
        deadline: String,
        selectedGroups: List<Group>,
        dialogBinding: DialogAssignmentBinding
    ): Boolean {
        resetAllErrors(dialogBinding)
        var isValid = true
        //Checking the title
        if (title.isBlank()) {
            showError(dialogBinding.etAssignmentTitleLayout, getString(R.string.enter_title_assignment))
            isValid = false
        }
        //Checking the description
        if (description.isBlank()) {
            showError(dialogBinding.etAssignmentDescriptionLayout, getString(R.string.enter_description_assignment))
            isValid = false
        }
        //Checking the subject
        if (subject == null) {
            showError(dialogBinding.spinnerSubjectLayout, getString(R.string.choose_subject_assignment))
            isValid = false
        }
        //Checking the deadline
        if (deadline.isBlank()) {
            showError(dialogBinding.etDeadlineLayout, getString(R.string.enter_deadline_assignment))
            isValid = false
        } else {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val deadlineDateTime = LocalDateTime.parse(deadline, formatter)
                if (deadlineDateTime.isBefore(LocalDateTime.now())) {
                    showError(dialogBinding.etDeadlineLayout, getString(R.string.past_deadline))
                    isValid = false
                }
            } catch (e: DateTimeParseException) {
                showError(dialogBinding.etDeadlineLayout, getString(R.string.error_date_format))
                isValid = false
            }
        }
        //Checking the groups
        if (selectedGroups.isEmpty()) {
            showError(dialogBinding.groupsSelectionLayout, getString(R.string.choose_at_least_one_group))
            isValid = false
        }
        return isValid
    }
    //reset all mark about error in filter
    private fun resetAllErrors(binding: DialogAssignmentBinding) {
        listOf(
            binding.etAssignmentTitleLayout,
            binding.etAssignmentDescriptionLayout,
            binding.spinnerSubjectLayout,
            binding.etDeadlineLayout,
            binding.groupsSelectionLayout
        ).forEach { layout ->
            layout.error = null
            layout.isErrorEnabled = false
        }
    }
    //function that mark error fields in filter
    private fun showError(layout: TextInputLayout?, message: String) {
        layout?.let {
            it.error = message
            it.isErrorEnabled = true
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