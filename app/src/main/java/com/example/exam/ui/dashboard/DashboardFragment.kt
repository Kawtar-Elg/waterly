package com.example.exam.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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
        // Progress bar setup
        binding.progressToday.max = 2000 // 2 liters in ml
        binding.progressToday.progress = 0
    }
    
    private fun loadTodayData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val totalConsumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, today)
                val totalConsumptionMl = (totalConsumption * 1000).toInt()
                
                binding.tvTodayAmount.text = "${String.format("%.1f", totalConsumption)} L"
                binding.progressToday.progress = totalConsumptionMl.coerceAtMost(2000)
                binding.tvProgressPercentage.text = "${((totalConsumption / 2.0) * 100).toInt()}%"
                
                // Update progress bar color based on achievement
                val progress = (totalConsumption / 2.0) * 100
                if (progress >= 100) {
                    binding.progressToday.progressTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#7ED321") // Green for goal achieved
                    )
                } else {
                    binding.progressToday.progressTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4A90E2") // Blue for in progress
                    )
                }
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadWeeklyData() {
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(Date().time - 6 * 24 * 60 * 60 * 1000L))
        
        lifecycleScope.launch {
            try {
                // Get weekly consumption data
                val weeklyConsumption = mutableListOf<BarEntry>()
                val dayLabels = mutableListOf<String>()
                
                for (i in 0..6) {
                    val date = Date(Date().time - (6 - i) * 24 * 60 * 60 * 1000L)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    val dayStr = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                    
                    val consumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, dateStr)
                    
                    weeklyConsumption.add(BarEntry(i.toFloat(), consumption.toFloat()))
                    dayLabels.add(dayStr.substring(0, 3)) // Short day name
                }
                
                setupWeeklyChart(weeklyConsumption, dayLabels)
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors du chargement du graphique", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupWeeklyChart(data: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(data, "Consommation d'eau (L)")
        dataSet.color = android.graphics.Color.parseColor("#4A90E2")
        dataSet.setDrawValues(false)
        
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        
        binding.chartWeekly.apply {
            this.data = barData
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            
            xAxis.apply {
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                textColor = android.graphics.Color.parseColor("#666666")
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 3f
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textColor = android.graphics.Color.parseColor("#666666")
            }
            
            axisRight.isEnabled = false
            
            invalidate() // Refresh chart
        }
    }
    
    private fun setupFloatingActionButton() {
        binding.fabAddWater.setOnClickListener {
            showAddWaterDialog()
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

                    // Refresh data
                    loadTodayData()
                    loadWeeklyData()
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