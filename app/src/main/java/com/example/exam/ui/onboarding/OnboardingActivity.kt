package com.waterly.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.waterly.databinding.ActivityOnboardingBinding
import com.waterly.ui.auth.AuthActivity
import com.waterly.data.adapter.OnboardingPagerAdapter

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
        
        TabLayoutMediator(binding.tabLayoutOnboarding, binding.viewPagerOnboarding) { _, _ -> }.attach()
        
        // Skip button
        binding.btnSkip.setOnClickListener {
            navigateToAuth()
        }
        
        // Get Started button (only visible on last page)
        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                
                if (position == 2) { // Last page
                    binding.btnSkip.text = "Commencez"
                } else {
                    binding.btnSkip.text = "Passer"
                }
            }
        })
        
        binding.btnSkip.setOnClickListener {
            if (binding.viewPagerOnboarding.currentItem == 2) {
                navigateToAuth()
            } else {
                binding.viewPagerOnboarding.currentItem = binding.viewPagerOnboarding.currentItem + 1
            }
        }
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