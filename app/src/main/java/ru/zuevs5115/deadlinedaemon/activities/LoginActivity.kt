package ru.zuevs5115.deadlinedaemon.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.database.AppDatabaseProvider
import ru.zuevs5115.deadlinedaemon.databinding.ActivityLoginBinding
import ru.zuevs5115.deadlinedaemon.entities.UserCredentials
import ru.zuevs5115.deadlinedaemon.utils.ProfileLog

//implement LoadingOverlayHandler to request Service show/hide our progress bar
class LoginActivity : AppCompatActivity(), LoadingOverlayHandler {
    //binding for edit all elements
    private lateinit var binding: ActivityLoginBinding
    //db
    private val userDao by lazy {
        AppDatabaseProvider.get(applicationContext).userDao()
    }
    private lateinit var allUsers: List<UserCredentials>
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
        //bind autoFill button
        binding.btnAutoFill.setOnClickListener {
            lifecycleScope.launch {
                //try get all save combinations
                val users = withContext(Dispatchers.IO) {
                    userDao.getAll()
                }
                //if have no users toast about it
                if (users.isEmpty()) {
                    Toast.makeText(this@LoginActivity, getString(R.string.have_not_saved), Toast.LENGTH_SHORT).show()
                } else {
                    val items = users.map { "${it.username} / ${it.password}" }
                    val adapter = ArrayAdapter(this@LoginActivity, android.R.layout.simple_list_item_1, items)
                    //set up dialog for choose combination
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                        .setTitle(getString(R.string.choose_user))
                        .setAdapter(adapter) { _, which ->
                            //try log in
                            val selected = users[which]
                            binding.etUsername.setText(selected.username)
                            binding.etPassword.setText(selected.password)
                            binding.btnLogin.performClick()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create()
                    //set up log click for delete
                    dialog.listView.setOnItemLongClickListener { _, _, position, _ ->
                        val userToDelete = users[position]
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle(getString(R.string.delete_entry))
                            .setMessage(getString(R.string.delete_confirmation, userToDelete.username))
                            .setPositiveButton(R.string.Yes) { _, _ ->
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        userDao.delete(userToDelete.username, userToDelete.password)
                                    }
                                    Toast.makeText(this@LoginActivity, getString(R.string.entry_deleted), Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                            }
                            .setNegativeButton(getString(R.string.No), null)
                            .show()
                        true
                    }
                    dialog.show()
                }
            }
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
