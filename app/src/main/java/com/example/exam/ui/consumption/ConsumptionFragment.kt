package com.example.exam.ui.consumption

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exam.R
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.databinding.FragmentConsumptionBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ConsumptionFragment : Fragment() {
    
    private var _binding: FragmentConsumptionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    private var currentFilter = "weekly"
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConsumptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        
        database = (requireActivity().application as WaterlyApp).database
        
        setupFilterButtons()
        setupCharts()
        loadData()
    }
    
    private fun setupFilterButtons() {
        binding.btnWeekly.setOnClickListener {
            selectFilter("weekly")
        }
        binding.btnMonthly.setOnClickListener {
            selectFilter("monthly")
        }
        binding.btnYearly.setOnClickListener {
            selectFilter("yearly")
        }
    }
    
    private fun selectFilter(filter: String) {
        currentFilter = filter
        
        // Reset all buttons
        binding.btnWeekly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
        binding.btnMonthly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
        binding.btnYearly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
        
        // Highlight selected button
        when (filter) {
            "weekly" -> binding.btnWeekly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4DD0E1"))
            "monthly" -> binding.btnMonthly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4DD0E1"))
            "yearly" -> binding.btnYearly.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4DD0E1"))
        }
        
        refreshAllData()
    }
    
    private fun setupCharts() {
        setupLineChart()
        setupBarChart()
    }
    
    private fun setupLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(Color.TRANSPARENT)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#4DD0E1")
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
    }
    
    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setBackgroundColor(Color.parseColor("#B3E5FC"))
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#1A237E")
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                textColor = Color.parseColor("#1A237E")
                setDrawGridLines(false)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }
    
    private fun loadData() {
        loadChartData()
        loadMonthlyStats()
    }
    
    private fun refreshAllData() {
        loadChartData()
        loadMonthlyStats()
    }
    
    private fun loadChartData() {
        lifecycleScope.launch {
            try {
                when (currentFilter) {
                    "weekly" -> loadWeeklyData()
                    "monthly" -> loadMonthlyData()
                    "yearly" -> loadYearlyData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun loadWeeklyData() {
        val calendar = Calendar.getInstance()
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        // Get last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            
            val consumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, date)
            
            entries.add(Entry((6 - i).toFloat(), consumption.toFloat()))
            labels.add(dayLabel)
        }
        
        updateLineChart(entries, labels, "Weekly Consumption")
    }
    
    private suspend fun loadMonthlyData() {
        val calendar = Calendar.getInstance()
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        // Get last 30 days
        for (i in 29 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayLabel = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            
            val consumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, date)
            
            entries.add(Entry((29 - i).toFloat(), consumption.toFloat()))
            labels.add(dayLabel)
        }
        
        updateLineChart(entries, labels, "Monthly Consumption")
    }
    
    private suspend fun loadYearlyData() {
        val calendar = Calendar.getInstance()
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        // Get last 12 months
        for (i in 11 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            
            val startDate = SimpleDateFormat("yyyy-MM-01", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            
            val consumption = database.waterConsumptionDao().getTotalConsumptionBetweenDates(currentUserId, startDate, endDate)
            
            entries.add(Entry((11 - i).toFloat(), consumption.toFloat()))
            labels.add(monthLabel)
        }
        
        updateLineChart(entries, labels, "Yearly Consumption")
    }
    
    private fun updateLineChart(entries: ArrayList<Entry>, labels: ArrayList<String>, label: String) {
        val dataSet = LineDataSet(entries, label).apply {
            color = Color.parseColor("#4DD0E1")
            setCircleColor(Color.parseColor("#4DD0E1"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = Color.WHITE
            setDrawFilled(true)
            fillColor = Color.parseColor("#4DD0E1")
            fillAlpha = 50
        }
        
        val lineData = LineData(dataSet)
        
        binding.lineChart.apply {
            data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }
    
    private fun loadMonthlyStats() {
        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val (startDate, endDate, daysInPeriod, currentDay) = when (currentFilter) {
                    "weekly" -> {
                        val start = calendar.clone() as Calendar
                        start.add(Calendar.DAY_OF_YEAR, -6)
                        val startStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(start.time)
                        val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        Tuple4(startStr, endStr, 7, 7)
                    }
                    "yearly" -> {
                        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                        val startStr = "$currentYear-01-01"
                        val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        Tuple4(startStr, endStr, 365, dayOfYear)
                    }
                    else -> { // monthly
                        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                        val startStr = "$currentMonth-01"
                        val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                        Tuple4(startStr, endStr, daysInMonth, currentDay)
                    }
                }
                
                val totalConsumption = database.waterConsumptionDao().getTotalConsumptionBetweenDates(currentUserId, startDate, endDate)
                val averagePerDay = if (currentDay > 0) totalConsumption / currentDay else 0.0
                
                // Count days with consumption >= 2L (goal achievement)
                var goalAchievementDays = 0
                val startCal = Calendar.getInstance()
                startCal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDate)!!
                
                for (i in 0 until currentDay) {
                    val dayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startCal.time)
                    val dayConsumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, dayDate)
                    if (dayConsumption >= 2.0) {
                        goalAchievementDays++
                    }
                    startCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                // Update UI with dynamic titles
                val (statsTitle, chartTitle) = when (currentFilter) {
                    "weekly" -> Pair("Weekly Stats", "This Week")
                    "yearly" -> Pair("Yearly Stats", "This Year")
                    else -> Pair("Monthly Stats", "This Month")
                }
                
                binding.tvStatsTitle.text = statsTitle
                binding.tvChartTitle.text = chartTitle
                binding.tvGoalAchievement.text = "$goalAchievementDays/$daysInPeriod"
                binding.tvProgressText.text = "$goalAchievementDays/$daysInPeriod\ndays"
                binding.tvTotalIntake.text = String.format("%.1f", totalConsumption)
                binding.tvAveragePerDay.text = String.format("%.1f", averagePerDay)
                
                val progressPercentage = ((goalAchievementDays.toFloat() / daysInPeriod) * 100).toInt()
                binding.progressGoal.progress = progressPercentage
                
                // Load bar chart
                loadMonthlyBarChart()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    
    private suspend fun loadMonthlyBarChart() {
        val calendar = Calendar.getInstance()
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        when (currentFilter) {
            "weekly" -> {
                for (i in 6 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val consumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, date)
                    entries.add(BarEntry((6 - i).toFloat(), consumption.toFloat()))
                    labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time))
                }
            }
            "yearly" -> {
                for (i in 11 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.MONTH, -i)
                    val startDate = SimpleDateFormat("yyyy-MM-01", Locale.getDefault()).format(calendar.time)
                    calendar.add(Calendar.MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val consumption = database.waterConsumptionDao().getTotalConsumptionBetweenDates(currentUserId, startDate, endDate)
                    entries.add(BarEntry((11 - i).toFloat(), consumption.toFloat()))
                    labels.add(SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time))
                }
            }
            else -> { // monthly
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                val startDay = maxOf(1, currentDay - 13)
                
                for (day in startDay..currentDay) {
                    val dayDate = String.format("%s-%02d", currentMonth, day)
                    val consumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, dayDate)
                    entries.add(BarEntry((day - startDay).toFloat(), consumption.toFloat()))
                    labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayDate)!!
                    ))
                }
            }
        }
        
        val dataSet = BarDataSet(entries, "Consumption").apply {
            color = Color.parseColor("#4DD0E1")
            valueTextSize = 8f
            valueTextColor = Color.parseColor("#1A237E")
        }
        
        val barData = BarData(dataSet)
        
        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}