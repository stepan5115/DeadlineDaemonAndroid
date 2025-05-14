package ru.zuevs5115.deadlinedaemon.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.adapters.SubjectAdapter
import ru.zuevs5115.deadlinedaemon.adapters.TokenAdapter
import ru.zuevs5115.deadlinedaemon.databinding.ActivityManagementBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogCreateAssignmentBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogDeleteAssignmentBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogSimpleInputBinding
import ru.zuevs5115.deadlinedaemon.databinding.DialogTokenBinding
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.EditData
import ru.zuevs5115.deadlinedaemon.utils.GetData
import ru.zuevs5115.deadlinedaemon.utils.Parser
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ManagementActivity : AppCompatActivity(), LoadingOverlayHandler {
    private lateinit var binding: ActivityManagementBinding
    private val FORMATER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private var clearSubjects: Subject = Subject(-1, "\uD83D\uDEABНе выбрано")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
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

    private fun showDeleteAssignmentFilterDialog() {
        val dialogBinding = DialogDeleteAssignmentBinding.inflate(LayoutInflater.from(this))

        // Загрузка данных
        if (SharedPrefs(this).getAllSubjects() == null) {
            //так как без предметов не может быть и заданий
            Toast.makeText(this, "Нету заданий в системе", Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!).toMutableList()
        subjects.add(clearSubjects)
        val selectedGroups: MutableList<Group> = mutableListOf()
        // Настройка Spinner для предметов
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            subjects.map { it.name }
        )
        dialogBinding.spinnerSubject.setAdapter(subjectAdapter)
        dialogBinding.spinnerSubject.setText(clearSubjects.name, false)

        val btnClearFilters: MaterialButton = dialogBinding.btnClearFilters
        btnClearFilters.setOnClickListener {
            dialogBinding.etDeadline.text?.clear()
        }

        // Настройка выбора групп
        dialogBinding.btnSelectGroups.setOnClickListener {
            GetData.getAllGroupsIndependenceUserForAssignmentContext(this, listOf(this::showGroupSelectionDialog), selectedGroups, dialogBinding)
        }

        // Настройка DatePicker для дедлайна
        dialogBinding.etDeadline.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Настройка фильтров")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("применить") { _, _ ->
                val title = dialogBinding.etAssignmentTitle.text.toString()
                val description = dialogBinding.etAssignmentDescription.text.toString()
                var subject = subjects.find { it.name == dialogBinding.spinnerSubject.text.toString() }
                if (subject == clearSubjects)
                    subject = null
                val deadline = TimeFormatter.fromStringSpaceToLocalDateTime(dialogBinding.etDeadline.text.toString())
                GetData.getAllAssignmentsIndependenceUserDeleteContext(this,
                    listOf(this::showDeleteAssignmentDialog),
                    title, description, subject, selectedGroups, deadline)
            }
            .create()
            .show()
    }

    private fun showGetTokensDialog() {
        val rawTokens = SharedPrefs(this).getTokens()
        if (rawTokens == null) {
            Toast.makeText(this, "Токенов не найдено", Toast.LENGTH_SHORT).show()
            return
        }

        val tokens = Parser.getAdminTokens(rawTokens).toMutableList()
        if (tokens.isEmpty()) {
            Toast.makeText(this, "У вас нет токенов", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = TokenAdapter(tokens) { token ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("token", token.token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Токен скопирован: ${token.token}", Toast.LENGTH_SHORT).show()
        }

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ManagementActivity)
            this.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ваши токены")
            .setView(recyclerView)
            .setNegativeButton("Закрыть", null)
            .create()

        adapter.setOnItemLongClickListener { token ->
            AlertDialog.Builder(this)
                .setTitle("Удалить токен?")
                .setMessage("Вы уверены, что хотите удалить этот токен?\n${token.token}")
                .setPositiveButton("Удалить") { _, _ ->
                    tokens.remove(token)
                    adapter.updateData(tokens)

                    // Обновление SharedPrefs
                    EditData.deleteToken(this, listOf {
                        Toast.makeText(this, "Токен удален: ${token.token}", Toast.LENGTH_SHORT).show()
                    }, token.id.toString())

                    // Закрытие основного диалога
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()
            true
        }

        dialog.show()
    }

    private fun showDeleteAssignmentDialog(title: String?, description: String?, subject: Subject?, groups: List<Group>,
                                           deadline: LocalDateTime?) {
        if (SharedPrefs(this).getAllAssignments() == null) {
            Toast.makeText(this, "Нету заданий в системе", Toast.LENGTH_SHORT).show()
            return
        }
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
                Toast.makeText(this, "Нету подходящих по фильтру заданий", Toast.LENGTH_SHORT).show()
                return
            }
            val assignmentTitles = assignments.map { it.title }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle("Удаление задания")
                .setItems(assignmentTitles) { _, which ->
                    deleteAssignment(assignments[which].id.toString())
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
        catch (e: Throwable) {
            Toast.makeText(this, "Ошибка при формировании списка заданий", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateToken() {
        EditData.generateTokenService(this, listOf(this::showTokenDialog))
    }

    private fun showCreateSubjectDialog() {
        val dialogBinding = DialogSimpleInputBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etInput.hint = "Введите название предмета"

        MaterialAlertDialogBuilder(this)
            .setTitle("Создание предмета")
            .setView(dialogBinding.root)
            .setPositiveButton("Создать") { _, _ ->
                val name = dialogBinding.etInput.text.toString()
                EditData.createSubject(this, listOf {
                    Toast.makeText(this, "ОК", Toast.LENGTH_SHORT).show()
                }, name)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteSubjectDialog() {
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, "В системе нету предметов", Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!).toList()
        val subjectNames = subjects.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление предмета")
            .setItems(subjectNames) { _, which ->
                deleteSubject(subjects[which].id.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showCreateGroupDialog() {
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
    }

    private fun showDeleteGroupDialog() {
        if (SharedPrefs(this).getAllGroups() == null) {
            Toast.makeText(this, "В системе нету групп", Toast.LENGTH_SHORT).show()
            return
        }
        val groups = Parser.fromJsonToGroups(SharedPrefs(this).getAllGroups()!!).toList()
        val groupNames = groups.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление группы")
            .setItems(groupNames) { _, which ->
                deleteGroup(groups[which].id.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTokenDialog(token: String) {
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
    }

    // Сигнатуры функций для вашей реализации
    private fun createAssignment(title: String, description: String, groupNames: List<String>, deadline: String, subject: Subject) {
        // Реализация создания задания
        EditData.createAssignmentService(this, listOf {
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }, title, description, Parser.groupNamesToJson(groupNames), deadline, subject.id.toString())
    }

    private fun deleteAssignment(assignmentId: String) {
        // Реализация удаления задания
        EditData.deleteAssignmentService(this, listOf {
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }, assignmentId)
    }

    private fun createSubject(name: String) {
        // Реализация создания предмета
    }

    private fun deleteSubject(subjectId: String) {
        // Реализация удаления предмета
        EditData.deleteSubject(this, listOf {
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }, subjectId)
    }

    private fun createGroup(name: String) {
        // Реализация создания группы
        EditData.createGroup(this, listOf {
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }, name)
    }

    private fun deleteGroup(groupId: String) {
        // Реализация удаления группы
        EditData.deleteGroup(this, listOf {
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }, groupId)
    }
    private fun showCreateAssignmentDialog() {
        val dialogBinding = DialogCreateAssignmentBinding.inflate(LayoutInflater.from(this))

        // Загрузка данных
        if (SharedPrefs(this).getAllSubjects() == null) {
            Toast.makeText(this, "Нету предметов в системе", Toast.LENGTH_SHORT).show()
            return
        }
        val subjects = Parser.fromJsonToSubjects(SharedPrefs(this).getAllSubjects()!!)
        val selectedGroups: MutableList<Group> = mutableListOf()
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

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Создание задания")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
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

// Добавляем кнопку "Создать" после создания, чтобы она была доступна в setOnShowListener
        dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Создать") { _, _ -> /* перехватываем */ }

        dialog.show()
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

    private fun validateAssignmentInput(
        title: String,
        description: String,
        subject: Subject?,
        deadline: String,
        selectedGroups: List<Group>,
        dialogBinding: DialogCreateAssignmentBinding
    ): Boolean {
        // Сбрасываем все ошибки перед новой проверкой
        resetAllErrors(dialogBinding)

        var isValid = true

        // Проверка заголовка
        if (title.isBlank()) {
            showError(dialogBinding.etAssignmentTitleLayout, "Введите название задания")
            isValid = false
        }

        // Проверка описания
        if (description.isBlank()) {
            showError(dialogBinding.etAssignmentDescriptionLayout, "Введите описание задания")
            isValid = false
        }

        // Проверка предмета
        if (subject == null) {
            showError(dialogBinding.spinnerSubjectLayout, "Выберите предмет")
            isValid = false
        }

        // Проверка дедлайна
        if (deadline.isBlank()) {
            showError(dialogBinding.etDeadlineLayout, "Укажите срок выполнения")
            isValid = false
        } else {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val deadlineDateTime = LocalDateTime.parse(deadline, formatter)
                if (deadlineDateTime.isBefore(LocalDateTime.now())) {
                    showError(dialogBinding.etDeadlineLayout, "Срок не может быть в прошлом")
                    isValid = false
                }
            } catch (e: DateTimeParseException) {
                showError(dialogBinding.etDeadlineLayout, "Неверный формат даты")
                isValid = false
            }
        }

        // Проверка групп
        if (selectedGroups.isEmpty()) {
            showError(dialogBinding.groupsSelectionLayout, "Выберите хотя бы одну группу")
            isValid = false
        }

        return isValid
    }

    private fun resetAllErrors(binding: DialogCreateAssignmentBinding) {
        listOf(
            binding.etAssignmentTitleLayout,
            binding.etAssignmentDescriptionLayout,
            binding.spinnerSubjectLayout,
            binding.etDeadlineLayout,
            binding.groupsSelectionLayout
        ).forEach { layout ->
            layout?.error = null
            layout?.isErrorEnabled = false
        }
    }

    private fun showError(layout: TextInputLayout?, message: String) {
        layout?.let {
            it.error = message
            it.isErrorEnabled = true
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