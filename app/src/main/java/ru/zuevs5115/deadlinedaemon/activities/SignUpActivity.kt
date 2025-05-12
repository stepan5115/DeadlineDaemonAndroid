package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ActivitySignupBinding
import ru.zuevs5115.deadlinedaemon.utils.ProfileLog

//implement LoadingOverlayHandler to request Service show/hide our progress bar
class SignUpActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding for edit all elements
    private lateinit var binding: ActivitySignupBinding
    //initial our UI
    override fun onCreate(savedInstanceState: Bundle?) {
        //base initial and remember binding
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set signUp button listener
        binding.btnSignUp.setOnClickListener {
            //get information for request
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            //check all information
            if (username.isBlank() || password.isBlank()) {
                binding.tvError.text = getString(R.string.get_username_and_password)
                return@setOnClickListener
            }
            //request to server
            ProfileLog.signUp(username, password, this, listOf(this::signUp), listOf(this::displayError))
        }
        //binding btnSwitchToLogIn
        binding.btnSwitchToLogin.setOnClickListener {
            //save username and password for next activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("username", binding.etUsername.text.toString())
            intent.putExtra("password", binding.etPassword.text.toString())
            startActivity(intent)
            finish()
        }
        //try get username and password from logIn activity if switch
        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        //try set this preferences
        binding.etUsername.setText(username)
        binding.etPassword.setText(password)
    }
    //listener for success request
    private fun signUp() {
        startActivity(Intent(this@SignUpActivity, ProfileInfoActivity::class.java))
        finish()
    }
    //listener for error request
    private fun displayError(error: String) {
        binding.tvError.text = error
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
