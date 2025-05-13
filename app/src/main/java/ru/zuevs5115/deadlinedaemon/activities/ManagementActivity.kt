package ru.zuevs5115.deadlinedaemon.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.zuevs5115.deadlinedaemon.databinding.ActivityManagementBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogCreateAssignmentBinding
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.utils.GetData
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import java.util.Date

class ManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.buttonCreateAssignment.setOnClickListener { GetData.getAllGroupsIndependenceUser(this,
            listOf(this::updateSubjectsAndTriggerAssignmentCreateDialog)) }
        binding.buttonDeleteAssignment.setOnClickListener { showDeleteAssignmentDialog() }
        binding.buttonGenerateToken.setOnClickListener { generateToken() }
        binding.buttonCreateSubject.setOnClickListener { showCreateSubjectDialog() }
        binding.buttonDeleteSubject.setOnClickListener { showDeleteSubjectDialog() }
        binding.buttonCreateGroup.setOnClickListener { showCreateGroupDialog() }
        binding.buttonDeleteGroup.setOnClickListener { showDeleteGroupDialog() }
    }

    private fun showDeleteAssignmentDialog() {
        /*
        val assignments = Parser.fromJsonToAssignments(SharedPrefs(this).getAllAssignments()!!)
        val assignmentTitles = assignments.map { it.title }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление задания")
            .setItems(assignmentTitles) { _, which ->
                deleteAssignment(assignments[which].id)
            }
            .setNegativeButton("Отмена", null)
            .show()
         */
    }

    private fun generateToken() {
        // Ваша реализация генерации токена
        // После генерации:
        // showTokenDialog(generatedToken)
    }

    private fun showCreateSubjectDialog() {
        /*
        val dialogBinding = DialogSimpleInputBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etInput.hint = "Введите название предмета"

        MaterialAlertDialogBuilder(this)
            .setTitle("Создание предмета")
            .setView(dialogBinding.root)
            .setPositiveButton("Создать") { _, _ ->
                val name = dialogBinding.etInput.text.toString()
                createSubject(name)
            }
            .setNegativeButton("Отмена", null)
            .show()
         */
    }

    private fun showDeleteSubjectDialog() {
        /*
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getSubjects()!!)
        val subjectNames = subjects.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление предмета")
            .setItems(subjectNames) { _, which ->
                deleteSubject(subjects[which].id)
            }
            .setNegativeButton("Отмена", null)
            .show()
         */
    }

    private fun showCreateGroupDialog() {
        /*
        val dialogBinding = DialogSimpleInputBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etInput.hint = "Введите название группы"

        MaterialAlertDialogBuilder(this)
            .setTitle("Создание группы")
            .setView(dialogBinding.root)
            .setPositiveButton("Создать") { _, _ ->
                val name = dialogBinding.etInput.text.toString()
                createGroup(name)
            }
            .setNegativeButton("Отмена", null)
            .show()
         */
    }

    private fun showDeleteGroupDialog() {
        /*
        val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!)
        val groupNames = groups.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление группы")
            .setItems(groupNames) { _, which ->
                deleteGroup(groups[which].id)
            }
            .setNegativeButton("Отмена", null)
            .show()
         */
    }

    fun showTokenDialog(token: String) {
        /*
        val dialogBinding = DialogTokenBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvToken.text = token

        dialogBinding.tvToken.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Token", token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Токен скопирован", Toast.LENGTH_SHORT).show()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Токен создан:")
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .show()
         */
    }

    // Сигнатуры функций для вашей реализации
    private fun createAssignment(title: String, description: String, groupIds: List<String>, subjectId: String, deadline: Date) {
        // Реализация создания задания
    }

    private fun deleteAssignment(assignmentId: String) {
        // Реализация удаления задания
    }

    private fun createSubject(name: String) {
        // Реализация создания предмета
    }

    private fun deleteSubject(subjectId: String) {
        // Реализация удаления предмета
    }

    private fun createGroup(name: String) {
        // Реализация создания группы
    }

    private fun deleteGroup(groupId: String) {
        // Реализация удаления группы
    }
    private fun showCreateAssignmentDialog() {
        val dialogBinding = DialogCreateAssignmentBinding.inflate(LayoutInflater.from(this))

        // Загрузка данных
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, "Нету предметов в системе", Toast.LENGTH_SHORT).show()
            return
        }
        if (SharedPrefs(this).getAllGroups() == null) {
            Toast.makeText(this, "Нету групп в системе", Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!)
        val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!)

        // Настройка Spinner для предметов
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            subjects.map { it.name }
        )
        dialogBinding.spinnerSubject.setAdapter(subjectAdapter)

        // Настройка выбора групп
        dialogBinding.btnSelectGroups.setOnClickListener {
            showGroupSelectionDialog(groups.toList(), dialogBinding)
        }

        // Настройка DatePicker для дедлайна
        dialogBinding.etDeadline.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Создание задания")
            .setView(dialogBinding.root)
            .setPositiveButton("Создать") { _, _ ->
                val title = dialogBinding.etAssignmentTitle.text.toString()
                val description = dialogBinding.etAssignmentDescription.text.toString()
                val subjectName = dialogBinding.spinnerSubject.text.toString()
                val deadline = dialogBinding.etDeadline.text.toString()
                /*
                // Валидация и создание задания
                if (validateAssignmentInput(title, description, subjectName, deadline)) {
                    val subject = subjects.find { it.name == subjectName }
                    createAssignment(title, description, selectedGroupIds, subject?.id, deadlineDate)
                }
                */
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showGroupSelectionDialog(
        groups: List<Group>,
        dialogBinding: DialogCreateAssignmentBinding
    ) {
        val groupNames = groups.map { it.name }.toTypedArray()
        val selectedIndices = mutableListOf<Int>() // Здесь нужно сохранять выбранные группы

        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите группы")
            .setMultiChoiceItems(groupNames, null) { _, which, isChecked ->
                if (isChecked) {
                    selectedIndices.add(which)
                } else {
                    selectedIndices.remove(which)
                }
            }
            .setPositiveButton("OK") { _, _ ->
                updateSelectedGroupsChips(groups, selectedIndices, dialogBinding)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateSelectedGroupsChips(
        groups: List<Group>,
        selectedIndices: List<Int>,
        dialogBinding: DialogCreateAssignmentBinding
    ) {
        /*
        dialogBinding.chipGroupSelected.removeAllViews()

        selectedIndices.forEach { index ->
            val chip = Chip(this).apply {
                text = groups[index].name
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    dialogBinding.chipGroupSelected.removeView(this)
                }
            }
            dialogBinding.chipGroupSelected.addView(chip)
        }
         */
    }

    private fun showDatePickerDialog(dialogBinding: DialogCreateAssignmentBinding) {
        /*
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дедлайн")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = dateFormatter.format(Date(selectedDate))
            dialogBinding.etDeadline.setText(dateString)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
         */
    }
    /*
    private fun validateAssignmentInput(
        title: String,
        description: String,
        subjectName: String,
        deadline: String
    ): Boolean {
        var isValid = true

        if (title.isEmpty()) {
            dialogBinding.etAssignmentTitle.error = "Введите заголовок"
            isValid = false
        }

        if (description.isEmpty()) {
            dialogBinding.etAssignmentDescription.error = "Введите описание"
            isValid = false
        }

        if (subjectName.isEmpty()) {
            dialogBinding.spinnerSubject.error = "Выберите предмет"
            isValid = false
        }

        if (deadline.isEmpty()) {
            dialogBinding.etDeadline.error = "Укажите дедлайн"
            isValid = false
        }
        return isValid
    }
     */
    private fun updateSubjectsAndTriggerAssignmentCreateDialog() {
        GetData.getAllSubjectsIndependenceUser(this, listOf(this::showCreateAssignmentDialog))
    }
}