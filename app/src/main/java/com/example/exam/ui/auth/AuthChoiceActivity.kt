package com.example.exam.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exam.databinding.ActivityAuthChoiceBinding

class AuthChoiceActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthChoiceBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityAuthChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("mode", "login")
            startActivity(intent)
        }
        
        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("mode", "signup")
            startActivity(intent)
        }
    }
}
