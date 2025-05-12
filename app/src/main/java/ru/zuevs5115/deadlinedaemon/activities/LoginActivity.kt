package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.utils.SharedPrefs
import ru.zuevs5115.deadlinedaemon.utils.ProfileLog

//implement LoadingOverlayHandler to request Service show/hide our progress bar
class LoginActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding for edit all elements
    private lateinit var binding: ActivityLoginBinding
    //initial our UI
    override fun onCreate(savedInstanceState: Bundle?) {
        //base initial and remember binding
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set logIn button listener
        binding.btnLogin.setOnClickListener {
            //get information for request
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            //check all information
            if (username.isBlank() || password.isBlank()) {
                binding.tvError.text = getString(R.string.get_username_and_password)
                return@setOnClickListener
            }
            //request to server
            ProfileLog.logIn(username, password, this, listOf(this::logIn), listOf(this::displayError))
        }
        //binding btnSwitchToSignUp
        binding.btnSwitchToSignUp.setOnClickListener {
            //save username and password for next activity
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("username", binding.etUsername.text.toString())
            intent.putExtra("password", binding.etPassword.text.toString())
            startActivity(intent)
            finish()
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
        //try get username and password from sign up activity if switch
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        //try set this preferences
        if (!username.isNullOrEmpty()) binding.etUsername.setText(username)
        if (!password.isNullOrEmpty()) binding.etPassword.setText(password)
    }
    //listener for success request
    private fun logIn() {
        startActivity(Intent(this@LoginActivity, ProfileInfoActivity::class.java))
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
