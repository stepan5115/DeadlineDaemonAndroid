package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authService = ApiClient.authService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                /*
                try {
                    val response = authService.signUp(username, password)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        // всё ок
                    } else {
                        binding.tvError.text = response.body()?.message ?: "Ошибка"
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.tvError.text = "Network error: ${e.message}"
                    }
                }
                */
            }
        }

        binding.btnSwitchToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        val (savedUser, savedPass) = SharedPrefs(this).getCredentials()
        if (!savedUser.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            // Автоматическая попытка входа
            binding.etUsername.setText(savedUser)
            binding.etPassword.setText(savedPass)
            binding.btnLogin.performClick()
        }
    }
}