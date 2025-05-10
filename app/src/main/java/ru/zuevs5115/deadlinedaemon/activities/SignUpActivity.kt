package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.databinding.ActivitySignupBinding
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val signUpService = ApiClient.signUpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                binding.tvError.text = "Введите имя пользователя и пароль"
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) { showLoading(true) }
                try {
                    val response = signUpService.signUp(username, password)
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        if (response.isSuccessful) {
                            val responseText = response.body()?.message ?: ""
                            if (responseText == "OK") {
                                SharedPrefs(this@SignUpActivity).saveCredentials(username, password)
                                startActivity(Intent(this@SignUpActivity, ProfileInfoActivity::class.java))
                                finish()
                            } else {
                                binding.tvError.text = responseText.replace("WRONG: ", "")
                            }
                        } else {
                            val errorMessage = ErrorHandler.handleError(response)
                            binding.tvError.text = errorMessage
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        binding.tvError.text = "Ошибка сети: ${e.message}"
                    }
                }
            }
        }

        binding.btnSwitchToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("username", binding.etUsername.text.toString())
            intent.putExtra("password", binding.etPassword.text.toString())
            startActivity(intent)
            finish()
        }

        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        binding.etUsername.setText(username)
        binding.etPassword.setText(password)
    }
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !show
        binding.etUsername.isEnabled = !show
        binding.etPassword.isEnabled = !show
        if (!show) binding.tvError.text = ""
    }
}
