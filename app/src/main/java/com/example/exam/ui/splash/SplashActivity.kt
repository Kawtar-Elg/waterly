package com.example.exam.ui.splash

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exam.R
import com.example.exam.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start splash animation
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            val ivSplash = findViewById<ImageView>(R.id.ivSplash)
            val tvAppName = findViewById<TextView>(R.id.tvAppName)
            
            // Set initial states
            ivSplash.alpha = 0f
            ivSplash.scaleX = 0.8f
            ivSplash.scaleY = 0.8f
            
            tvAppName.alpha = 0f
            tvAppName.scaleX = 0.8f
            tvAppName.scaleY = 0.8f

            delay(300)

            // Splash circle animation
            ivSplash.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start()

            // App name animation
            tvAppName.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(300)
                .start()

            delay(2500)

            // Navigate to next screen
            checkUserSession()
        }
    }

    private suspend fun checkUserSession() {
        withContext(Dispatchers.IO) {
            delay(500)

            withContext(Dispatchers.Main) {
                // Navigate to onboarding
                val intent = Intent(this@SplashActivity, com.example.exam.ui.onboarding.OnboardingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
