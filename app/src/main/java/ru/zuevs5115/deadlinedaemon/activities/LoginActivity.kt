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
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.api.ApiClient
import ru.zuevs5115.deadlinedaemon.utils.ErrorHandler

class LoginActivity : AppCompatActivity() {
    //binding for edit all elements
    private lateinit var binding: ActivityLoginBinding
    //service for requesting/responding
    private val authService = ApiClient.authService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
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
                    val response = authService.login(username, password)
                    //set coroutine thread to Android Main for edit UI (only main can do that)
                    withContext(Dispatchers.Main) {
                        //hide load bar
                        showLoading(false)
                        if (response.isSuccessful) {
                            //set activity or set error textView
                            val responseText = response.body()?.message ?: ""
                            if (responseText == "OK") {
                                SharedPrefs(this@LoginActivity).saveCredentials(username, password)
                                startActivity(Intent(this@LoginActivity, ProfileInfoActivity::class.java))
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
        //binding btnSwitchToSignUp
        binding.btnSwitchToSignUp.setOnClickListener {
            //save username and password for next activity
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("username", binding.etUsername.text.toString())
            intent.putExtra("password", binding.etPassword.text.toString())
            startActivity(intent)
        }
        //try get shared preferences
        val (savedUser, savedPass) = SharedPrefs(this).getCredentials()
        //try set all editText
        if (!savedUser.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            binding.etUsername.setText(savedUser)
            binding.etPassword.setText(savedPass)
            //try log-in
            binding.btnLogin.performClick()
        }
        //try get username and password from another activity
        //just in case the last activity is not filled with SharedPrefs
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        //try set this preferences
        if (!username.isNullOrEmpty()) binding.etUsername.setText(username)
        if (!password.isNullOrEmpty()) binding.etPassword.setText(password)
    }
    //add process bar on/off functions
    private fun showLoading(show: Boolean) {
        //set visibility
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        //set accessibility of all interaction objects
        binding.btnLogin.isEnabled = !show
        binding.etUsername.isEnabled = !show
        binding.etPassword.isEnabled = !show
        //hide all errors if process
        if (!show) binding.tvError.text = ""
    }
}
