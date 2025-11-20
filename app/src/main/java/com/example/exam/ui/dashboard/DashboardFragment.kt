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
        
        setupViews()
        loadTodayData()
        loadWeeklyData()
        setupFloatingActionButton()
    }
    
    private fun setupViews() {
        // Setup Lottie animation
        try {
            binding.lottieBottle.setAnimation(R.raw.animationbottle)
            binding.lottieBottle.repeatCount = ValueAnimator.INFINITE
            binding.lottieBottle.loop(true)
            binding.lottieBottle.playAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Tab buttons
        binding.btnConsumption.setOnClickListener {
            Toast.makeText(requireContext(), "Consommation", Toast.LENGTH_SHORT).show()
        }
        binding.btnGraph.setOnClickListener {
            Toast.makeText(requireContext(), "Graphique", Toast.LENGTH_SHORT).show()
        }
        binding.btnAdvice.setOnClickListener {
            Toast.makeText(requireContext(), "Conseils", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadTodayData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val totalConsumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, today)
                val goalLiters = 2.0
                val percentage = ((totalConsumption / goalLiters) * 100).toInt().coerceAtMost(100)
                val remaining = (goalLiters - totalConsumption).coerceAtLeast(0.0)
                
                // Update daily goal
                binding.tvDailyGoal.text = "${String.format("%.1f", totalConsumption)} Litres"
                
                // Update consumption card
                binding.tvConsumptionAmount.text = "${String.format("%.1f", totalConsumption)}L $percentage%"
                
                // Update circular progress
                binding.circularProgress.progress = percentage
                binding.tvCircularPercentage.text = "$percentage%"
                
                // Update bottle percentage text
                binding.tvBottlePercentage.text = "$percentage%"
                
                // Update Lottie animation progress based on percentage
                val progress = percentage / 100f
                binding.lottieBottle.progress = progress
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadWeeklyData() {
        // Weekly data loading removed for simplified dashboard
    }
    
    private fun setupFloatingActionButton() {
        var dX = 0f
        var dY = 0f
        var lastAction = 0

        binding.fabAddWater.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    lastAction = android.view.MotionEvent.ACTION_DOWN
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    lastAction = android.view.MotionEvent.ACTION_MOVE
                }
                android.view.MotionEvent.ACTION_UP -> {
                    if (lastAction == android.view.MotionEvent.ACTION_DOWN) {
                        showAddWaterDialog()
                    }
                }
            }
            true
        }
    }
    
    private fun showAddWaterDialog() {
        val amounts = arrayOf("250ml", "500ml", "750ml", "1L")
        val values = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter de l'eau")
            .setItems(amounts) { _, which ->
                addWaterConsumption(values[which])
            }
            .show()
    }
    
    private fun addWaterConsumption(amount: Float) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val consumption = WaterConsumption(
                    userId = currentUserId,
                    amount = amount.toDouble(),
                    date = today
                )
                
                database.waterConsumptionDao().insertConsumption(consumption)
                
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "${amount}L ajoutés!", Toast.LENGTH_SHORT).show()
                    loadTodayData()
                }
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}