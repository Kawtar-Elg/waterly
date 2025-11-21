package com.example.exam.ui.dashboard

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.example.exam.R
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.data.entity.WaterConsumption
import com.example.exam.databinding.FragmentDashboardBinding
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get user ID from preferences
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        
        database = (requireActivity().application as WaterlyApp).database
        
        setupViewPager()
        setupViews()
        setupAddObjectiveButton()
        
        // Initialize button states
        updateTabButtons(0)
        updateDotIndicators(0)
    }
    
    private fun setupViews() {
        animateTopCard()
        animateTabButtons()
        
        binding.btnConsumption.setOnClickListener {
            animateButtonClick(it)
            binding.viewPager.currentItem = 0
        }
        binding.btnGraph.setOnClickListener {
            animateButtonClick(it)
            binding.viewPager.currentItem = 1
        }
        binding.btnAdvice.setOnClickListener {
            animateButtonClick(it)
            binding.viewPager.currentItem = 2
        }
    }
    
    private fun animateTopCard() {
        binding.topCard.alpha = 0f
        binding.topCard.translationY = -50f
        binding.topCard.animate().alpha(1f).translationY(0f).setDuration(500).start()
    }
    
    private fun animateTabButtons() {
        binding.tabIndicator.alpha = 0f
        binding.tabScroll.alpha = 0f
        binding.tabIndicator.animate().alpha(1f).setDuration(400).setStartDelay(300).start()
        binding.tabScroll.animate().alpha(1f).setDuration(400).setStartDelay(400).start()
    }
    
    private fun animateButtonClick(view: View) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    
    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.setPageTransformer(ZoomOutPageTransformer())
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabButtons(position)
                updateDotIndicators(position)
            }
        })
    }
    
    private class ZoomOutPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> alpha = 0f
                    position <= 1 -> {
                        val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
                        val vertMargin = pageWidth * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        alpha = 0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f
                    }
                    else -> alpha = 0f
                }
            }
        }
    }
    
    private fun updateTabButtons(position: Int) {
        when (position) {
            0 -> {
                animateButtonTransition(binding.btnConsumption, true)
                animateButtonTransition(binding.btnGraph, false)
                animateButtonTransition(binding.btnAdvice, false)
            }
            1 -> {
                animateButtonTransition(binding.btnGraph, true)
                animateButtonTransition(binding.btnConsumption, false)
                animateButtonTransition(binding.btnAdvice, false)
            }
            2 -> {
                animateButtonTransition(binding.btnAdvice, true)
                animateButtonTransition(binding.btnConsumption, false)
                animateButtonTransition(binding.btnGraph, false)
            }
        }
    }
    
    private fun animateButtonTransition(button: android.widget.Button, isActive: Boolean) {
        button.animate().scaleX(if (isActive) 1.05f else 1f).scaleY(if (isActive) 1.05f else 1f).setDuration(200).start()
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isActive) "#4DD0E1" else "#00000000")
        )
        button.setTextColor(android.graphics.Color.parseColor(if (isActive) "#1A237E" else "#FFFFFF"))
    }
    
    private inner class DashboardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardContentFragment()
                1 -> com.example.exam.ui.consumption.ConsumptionFragment()
                2 -> com.example.exam.ui.tips.TipsFragment()
                else -> DashboardContentFragment()
            }
        }
    }
    
    private fun setupAddObjectiveButton() {
        binding.btnAddObjective.setOnClickListener {
            showAddWaterDialog()
        }
    }
    
    private fun showAddWaterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_water, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_250ml).setOnClickListener {
            addWaterConsumption(0.25f)
            dialog.dismiss()
        }
        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_500ml).setOnClickListener {
            addWaterConsumption(0.5f)
            dialog.dismiss()
        }
        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_750ml).setOnClickListener {
            addWaterConsumption(0.75f)
            dialog.dismiss()
        }
        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_1l).setOnClickListener {
            addWaterConsumption(1.0f)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun addWaterConsumption(amount: Float) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val consumption = com.example.exam.data.entity.WaterConsumption(
                    userId = currentUserId,
                    amount = amount.toDouble(),
                    date = today
                )
                
                database.waterConsumptionDao().insertConsumption(consumption)
                
                if (isAdded && context != null) {
                    android.widget.Toast.makeText(requireContext(), "${amount}L ajoutés!", android.widget.Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    android.widget.Toast.makeText(requireContext(), "Erreur lors de l'ajout", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateDotIndicators(position: Int) {
        when (position) {
            0 -> {
                binding.dot1.animate().scaleX(2f).setDuration(200).start()
                binding.dot2.animate().scaleX(1f).setDuration(200).start()
                binding.dot3.animate().scaleX(1f).setDuration(200).start()
            }
            1 -> {
                binding.dot1.animate().scaleX(1f).setDuration(200).start()
                binding.dot2.animate().scaleX(2f).setDuration(200).start()
                binding.dot3.animate().scaleX(1f).setDuration(200).start()
            }
            2 -> {
                binding.dot1.animate().scaleX(1f).setDuration(200).start()
                binding.dot2.animate().scaleX(1f).setDuration(200).start()
                binding.dot3.animate().scaleX(2f).setDuration(200).start()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}