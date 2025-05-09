package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityProfileBinding
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import org.json.JSONObject
import ru.zuevs5115.deadlinedaemon.utils.TimeFormatter

class ProfileInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val infoService = ApiClient.getInfoService
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout

        // Настройка кнопки меню в тулбаре
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu) // Иконка меню (бургер)
        }

        setupNavigation()
        updateProfileData()
    }

    private fun setupNavigation() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Уже на экране профиля
                }
                R.id.nav_tasks -> {
                    // startActivity(Intent(this, TasksActivity::class.java))
                }
                R.id.nav_settings -> {
                    // startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_refresh -> {
                    updateProfileData()
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Обработка нажатия на иконку меню в тулбаре
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_refresh -> {
                updateProfileData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    private fun loadProfileData(jsonString: String) {
        try {
            val json = JSONObject(jsonString)

            // Основная информация
            binding.tvUserId.text = json.getInt("user_id").toString()
            binding.tvUsername.text = json.getString("username")
            binding.tvGroups.text = json.getJSONArray("groups").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }

            // Настройки
            binding.tvCanEditTasks.text = if (json.getBoolean("canEditTasks")) "Да" else "Нет"
            binding.tvAllowNotifications.text = if (json.getBoolean("allowNotifications")) "Включены" else "Выключены"

            // Конвертируем секунды в минуты для интервала уведомлений
            val seconds = if (json.has("notificationIntervalSeconds")) json.getInt("notificationIntervalSeconds") else null
            binding.tvNotificationInterval.text = TimeFormatter.formatNotificationInterval(seconds)

            // Исключенные предметы
            binding.tvExcludedSubjects.text = json.getJSONArray("notificationExcludedSubjects").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }

            // Выполненные задания
            binding.tvCompletedAssignments.text = json.getJSONArray("completedAssignments").let { array ->
                (0 until array.length()).joinToString { array.getString(it) }
            }

            // Обновляем имя пользователя в меню
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvMenuUsername).text =
                json.getString("username")

        } catch (e: Exception) {
            Toast.makeText(this@ProfileInfoActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    private fun updateProfileData() {
        val (savedUser, savedPass) = SharedPrefs(this).getCredentials()
        if (savedUser != null && savedPass != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = infoService.getInfo(savedUser, savedPass)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val responseText = response.body()?.message ?: ""
                            loadProfileData(responseText)
                        } else {
                            val errorMessage = ErrorHandler.handleError(response)
                            Toast.makeText(this@ProfileInfoActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ProfileInfoActivity, LoginActivity::class.java))
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("ProfileInfo", "Network error", e)
                        Toast.makeText(this@ProfileInfoActivity, "Network error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun logout() {
        SharedPrefs(this).clearCredentials()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}