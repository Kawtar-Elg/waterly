package com.example.exam.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.exam.R
import com.example.exam.ui.onboarding.OnboardingFragment

class OnboardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val onboardingPages = listOf(
        Triple("Welcome to\nwaterly !", "Stay hydrated and never miss\na sip !", R.drawable.image1ona1),
        Triple("Track Your Water\nIntake", "Monitor your daily hydration\ngoals easily", R.drawable.image1ona1),
        Triple("Get Reminders", "Never forget to drink water\nthroughout the day", R.drawable.image1ona1)
    )

    override fun getItemCount(): Int = onboardingPages.size

    override fun createFragment(position: Int): Fragment {
        val (title, description, imageResId) = onboardingPages[position]
        return OnboardingFragment.newInstance(title, description, imageResId)
    }
}