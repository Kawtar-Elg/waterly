package com.example.exam.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.exam.R
import com.example.exam.ui.onboarding.OnboardingFragment


class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(
                title = "Welcome to\nwaterly !",
                description = "Stay hydrated and never miss\na sip !",
                imageResId = R.drawable.ic_water_character
            )
            1 -> OnboardingFragment.newInstance(
                title = "Why Stay\nHydrated?",
                description = "Water boosts your energy, improves skin,\nand keeps your mind fresh!",
                imageResId = R.drawable.ic_water_character
            )
            2 -> OnboardingFragment.newInstance(
                title = "Reminders\nOn?",
                description = "Allow notifications to get your daily\nwater intake reminders",
                imageResId = R.drawable.ic_water_character
            )
            else -> OnboardingFragment()
        }
    }
}