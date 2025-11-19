package com.example.exam.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.example.exam.databinding.ActivityOnboardingBinding
import com.example.exam.ui.auth.AuthActivity
import com.example.exam.data.adapter.OnboardingPagerAdapter

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupOnboarding()
    }
    
    private fun setupOnboarding() {
        val onboardingAdapter = OnboardingPagerAdapter(this)
        
        binding.viewPagerOnboarding.apply {
            adapter = onboardingAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        
        // Setup dots indicator
        TabLayoutMediator(binding.tabLayoutOnboarding, binding.viewPagerOnboarding) { _, _ -> }.attach()
        
        // Next button click
        binding.btnNext.setOnClickListener {
            if (binding.viewPagerOnboarding.currentItem == 2) {
                // Last page - navigate to auth
                navigateToAuth()
            } else {
                // Go to next page
                binding.viewPagerOnboarding.currentItem = binding.viewPagerOnboarding.currentItem + 1
            }
        }
        
        // Update button text on page change
        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                
                if (position == 2) {
                    binding.btnNext.text = "Let's Go !"
                } else {
                    binding.btnNext.text = "→"
                }
            }
        })
    }
    
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        if (binding.viewPagerOnboarding.currentItem > 0) {
            binding.viewPagerOnboarding.currentItem = binding.viewPagerOnboarding.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }
}