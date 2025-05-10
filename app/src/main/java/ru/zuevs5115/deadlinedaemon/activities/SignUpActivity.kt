package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.databinding.ActivitySignupBinding
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs

class SignUpActivity : AppCompatActivity() {
    //binding for edit all elements
    private lateinit var binding: ActivitySignupBinding
    //service for requesting/responding
    private val signUpService = ApiClient.signUpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            //get information for request
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            //check all information
            if (username.isBlank() || password.isBlank()) {
                binding.tvError.text = getString(R.string.get_username_and_password)
                return@setOnClickListener
            }
            //coroutine for async
            CoroutineScope(Dispatchers.IO).launch {
                //start load bar
                withContext(Dispatchers.Main) { showLoading(true) }
                try {
                    //get response
                    val response = signUpService.signUp(username, password)
                    //set coroutine thread to Android Main for edit UI (only main can do that)
                    withContext(Dispatchers.Main) {
                        //hide load bar
                        showLoading(false)
                        if (response.isSuccessful) {
                            //set activity or set error textView
                            val responseText = response.body()?.message ?: ""
                            if (responseText == "OK") {
                                SharedPrefs(this@SignUpActivity).saveCredentials(username, password)
                                startActivity(Intent(this@SignUpActivity, ProfileInfoActivity::class.java))
                                finish()
                            } else {
                                binding.tvError.text = responseText.replace("WRONG: ", "")
                            }
                        } else {
                            //processing error
                            val errorMessage = ErrorHandler.handleError(response)
                            binding.tvError.text = errorMessage
                        }
                    }

                } catch (e: Exception) {
                    //processing error
                    withContext(Dispatchers.Main) {
                        //hide process bar
                        showLoading(false)
                        //set error text
                        binding.tvError.text = getString(R.string.network_error, e.message)
                    }
                }
            }
        }

        binding.btnSwitchToLogin.setOnClickListener {
            //save username and password for next activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("username", binding.etUsername.text.toString())
            intent.putExtra("password", binding.etPassword.text.toString())
            startActivity(intent)
            finish()
        }
        //try get username and password from another activity
        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        //try set this preferences
        binding.etUsername.setText(username)
        binding.etPassword.setText(password)
    }
    //add process bar on/off functions
    private fun showLoading(show: Boolean) {
        //set visibility
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        //set accessibility of all interaction objects
        binding.btnSignUp.isEnabled = !show
        binding.etUsername.isEnabled = !show
        binding.etPassword.isEnabled = !show
        //hide all errors if process
        if (!show) binding.tvError.text = ""
    }
}
