package com.example.exam.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.exam.R
import com.example.exam.data.adapter.OnboardingPagerAdapter
import com.example.exam.ui.auth.AuthChoiceActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                btnGetStarted.visibility = if (position == 2) View.VISIBLE else View.GONE
            }
        })

        btnGetStarted.setOnClickListener {
            getSharedPreferences("waterly_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply()
            
            startActivity(Intent(this, AuthChoiceActivity::class.java))
            finish()
        }
    }
}