package com.example.exam.ui.splash

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exam.R
import com.waterly.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// fin kayn imorta xml ?? idk
class SplashActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo)
        tvAppName = findViewById(R.id.tvAppName)
        progressBar = findViewById(R.id.progressBar)

        // Start splash animation
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            // Animate the water drop logo
            ivLogo.alpha = 0f
            ivLogo.scaleX = 0.5f
            ivLogo.scaleY = 0.5f

            tvAppName.alpha = 0f
            tvAppName.translationY = 50f

            progressBar.alpha = 0f

            // Delay for 500ms before starting animations
            delay(500)

            // Animate logo entrance
            ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start()

            // Animate app name
            tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(300)
                .start()

            // Animate progress bar
            progressBar.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(800)
                .start()

            // Progress bar animation
            progressBar.progress = 0
            progressBar.max = 100

            // Animate progress
            for (i in 0..100 step 5) {
                progressBar.progress = i
                delay(30)
            }

            // Navigate to next screen
            checkUserSession()
        }
    }

    private suspend fun checkUserSession() {
        withContext(Dispatchers.IO) {
            delay(1000)

            // Here you would check if user is logged in
            // For now, we'll always go to AuthActivity
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashActivity, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
