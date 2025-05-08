package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authService = ApiClient.authService
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            Log.d(TAG, "Login button clicked with username='$username', password='${"*".repeat(password.length)}'")

            if (username.isBlank() || password.isBlank()) {
                binding.tvError.text = "Введите имя пользователя и пароль"
                Log.d(TAG, "Empty username or password")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = authService.login(username, password)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val responseText = response.body()?.message ?: ""
                            if (responseText == "OK") {
                                SharedPrefs(this@LoginActivity).saveCredentials(username, password)
                                startActivity(Intent(this@LoginActivity, ProfileInfoActivity::class.java))
                                finish()
                            } else {
                                binding.tvError.text = responseText.replace("WRONG: ", "")
                            }
                        } else {
                            // Используем ErrorHandler для обработки ошибок
                            val errorMessage = ErrorHandler.handleError(response)
                            binding.tvError.text = errorMessage
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Network error", e)
                    withContext(Dispatchers.Main) {
                        binding.tvError.text = "Ошибка сети: ${e.message}"
                    }
                }
            }
        }

        binding.btnSwitchToSignUp.setOnClickListener {
            Log.d(TAG, "Switching to SignUpActivity")
            startActivity(Intent(this, SignUpActivity::class.java))
        }

//        val (savedUser, savedPass) = SharedPrefs(this).getCredentials()
//        if (!savedUser.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
//            Log.d(TAG, "Saved credentials found: username='$savedUser', password='${"*".repeat(savedPass.length)}'")
//            binding.etUsername.setText(savedUser)
//            binding.etPassword.setText(savedPass)
//            binding.btnLogin.performClick()
//        } else {
//            Log.d(TAG, "No saved credentials found")
//        }
    }
}
