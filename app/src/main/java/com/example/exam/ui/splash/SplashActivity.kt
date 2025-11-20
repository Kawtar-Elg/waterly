package com.example.exam.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.exam.MainActivity
import com.example.exam.R
import com.example.exam.ui.auth.AuthChoiceActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.splashscreen}")
        
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            videoView.start()
        }
        videoView.setOnCompletionListener {
            navigateToNext()
        }
        videoView.setOnErrorListener { _, _, _ ->
            navigateToNext()
            true
        }
    }

    private fun navigateToNext() {
        val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        val currentUserId = prefs.getLong("current_user_id", 0)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
        
        val intent = when {
            currentUserId != 0L -> Intent(this, MainActivity::class.java)
            onboardingCompleted -> Intent(this, AuthChoiceActivity::class.java)
            else -> Intent(this, com.example.exam.ui.onboarding.OnboardingActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
