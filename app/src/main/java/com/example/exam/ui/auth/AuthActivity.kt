package com.example.exam.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.exam.MainActivity
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.data.entity.User
import com.example.exam.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = (application as WaterlyApp).database
        
        setupViews()
        setupListeners()
    }
    
    private fun setupViews() {
        // Set initial state to login
        showLoginView()
    }
    
    private fun setupListeners() {
        // Toggle between login and signup
        binding.tvToggleMode.setOnClickListener {
            if (binding.tilEmail.visibility == View.VISIBLE) {
                showSignupView()
            } else {
                showLoginView()
            }
        }
        
        // Login button
        binding.btnLogin.setOnClickListener {
            performLogin()
        }
        
        // Signup button
        binding.btnSignup.setOnClickListener {
            performSignup()
        }
    }
    
    private fun showLoginView() {
        binding.tilEmail.visibility = View.VISIBLE
        binding.tilPassword.visibility = View.VISIBLE
        binding.cbPrivacy.visibility = View.VISIBLE
        binding.btnLogin.visibility = View.VISIBLE
        binding.btnGoogle.visibility = View.VISIBLE
        binding.clSignupView.visibility = View.GONE
        binding.tvToggleMode.text = "Don't have an account ? Sign up"
    }
    
    private fun showSignupView() {
        binding.tilEmail.visibility = View.GONE
        binding.tilPassword.visibility = View.GONE
        binding.cbPrivacy.visibility = View.GONE
        binding.btnLogin.visibility = View.GONE
        binding.btnGoogle.visibility = View.GONE
        binding.clSignupView.visibility = View.VISIBLE
        binding.tvToggleMode.text = "Already have an account ? Log in"
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val user = database.userDao().getUserByEmail(email)
                if (user != null && user.password == password) {
                    // Save current user ID to preferences
                    val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
                    prefs.edit().putLong("current_user_id", user.id).apply()
                    
                    Toast.makeText(this@AuthActivity, "Connexion réussie!", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@AuthActivity, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun performSignup() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmailSignup.text.toString().trim()
        val password = binding.etPassword1.text.toString()
        val password2 = binding.etPassword2.text.toString()
        val birthday = binding.etBirthday.text.toString().trim()
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != password2) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check age (must be 18+)
        if (!isUserAdult(birthday)) {
            Toast.makeText(this, "Vous devez avoir au moins 18 ans", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val existingUser = database.userDao().getUserByEmail(email)
                if (existingUser != null) {
                    Toast.makeText(this@AuthActivity, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val user = User(
                    username = username,
                    email = email,
                    password = password,
                    birthday = birthday
                )
                
                val userId = database.userDao().insertUser(user)
                
                // Save current user ID to preferences
                val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
                prefs.edit().putLong("current_user_id", userId).apply()
                
                Toast.makeText(this@AuthActivity, "Compte créé avec succès!", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this@AuthActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun isUserAdult(birthday: String): Boolean {
        try {
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val birthDate = dateFormat.parse(birthday)
            val currentDate = java.util.Date()
            
            if (birthDate != null) {
                val ageInMillis = currentDate.time - birthDate.time
                val ageInYears = ageInMillis / (1000L * 60 * 60 * 24 * 365)
                return ageInYears >= 18
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
}