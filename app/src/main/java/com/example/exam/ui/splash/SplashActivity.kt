package com.example.exam.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.exam.R

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
        val intent = Intent(this@SplashActivity, com.example.exam.ui.onboarding.OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}
