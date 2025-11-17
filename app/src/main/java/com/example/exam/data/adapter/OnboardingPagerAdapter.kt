package com.waterly.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.waterly.ui.onboarding.OnboardingFragment
import com.waterly.R

class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(
                title = "Bienvenue sur Waterly!",
                description = "Restez hydraté et ne manquez jamais une gorgée!",
                imageResId = R.drawable.ic_water_drop,
                backgroundColor = R.color.splash_background_start
            )
            1 -> OnboardingFragment.newInstance(
                title = "Suivez votre hydratation",
                description = "Enregistrez votre consommation d'eau quotidienne et suivez vos progrès.",
                imageResId = R.drawable.ic_chart,
                backgroundColor = R.color.splash_background_start
            )
            2 -> OnboardingFragment.newInstance(
                title = "Atteignez vos objectifs",
                description = "Définissez des objectifs personnalisés et recevez des rappels réguliers.",
                imageResId = R.drawable.ic_goal,
                backgroundColor = R.color.splash_background_start
            )
            else -> OnboardingFragment()
        }
    }
}