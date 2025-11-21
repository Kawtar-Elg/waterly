package com.example.exam.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exam.R
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.data.entity.User
import com.example.exam.databinding.FragmentGoalsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GoalsFragment : Fragment() {
    
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    private var currentUser: User? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        
        database = (requireActivity().application as WaterlyApp).database
        
        animateViews()
        setupViews()
        loadUserData()
        setupListeners()
    }
    
    private fun animateViews() {
        binding.cardCurrentGoal.alpha = 0f
        binding.cardCurrentGoal.scaleX = 0.9f
        binding.cardCurrentGoal.scaleY = 0.9f
        binding.cardCurrentGoal.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start()
        
        binding.cardSetGoal.alpha = 0f
        binding.cardSetGoal.translationY = 50f
        binding.cardSetGoal.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start()
        
        val buttons = listOf(binding.btnGoal1, binding.btnGoal2, binding.btnGoal3, binding.btnGoal4)
        buttons.forEachIndexed { index, button ->
            button.alpha = 0f
            button.scaleX = 0.8f
            button.scaleY = 0.8f
            button.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).setStartDelay((index * 100 + 400).toLong()).start()
        }
    }
    
    private fun setupViews() {
        binding.btnGoal1.setOnClickListener { 
            animateButtonClick(it)
            setDailyGoal(1.5) 
        }
        binding.btnGoal2.setOnClickListener { 
            animateButtonClick(it)
            setDailyGoal(2.0) 
        }
        binding.btnGoal3.setOnClickListener { 
            animateButtonClick(it)
            setDailyGoal(2.5) 
        }
        binding.btnGoal4.setOnClickListener { 
            animateButtonClick(it)
            setDailyGoal(3.0) 
        }
    }
    
    private fun animateButtonClick(view: View) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }
    
    private fun setupListeners() {
        binding.btnSaveGoal.setOnClickListener {
            animateButtonClick(it)
            saveDailyGoal()
        }
        
        binding.seekBarGoal.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val goal = 1.0 + (progress / 100.0) * 2.0 // 1.0L to 3.0L
                    binding.tvGoalValue.text = "${String.format("%.1f", goal)}L"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                currentUser = database.userDao().getUserById(currentUserId)
                loadCurrentGoal()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors du chargement des données utilisateur", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadCurrentGoal() {
        val currentGoal = currentUser?.dailyGoal ?: 2.0
        binding.tvCurrentGoal.text = "${String.format("%.1f", currentGoal)}L"
        binding.seekBarGoal.progress = ((currentGoal - 1.0) / 2.0 * 100).toInt()
        binding.tvGoalValue.text = "${String.format("%.1f", currentGoal)}L"
        
        // Update progress indicator
        updateGoalProgress()
    }
    
    private fun setDailyGoal(goal: Double) {
        binding.seekBarGoal.progress = ((goal - 1.0) / 2.0 * 100).toInt()
        binding.tvGoalValue.text = "${String.format("%.1f", goal)}L"
        saveGoalToDatabase(goal)
    }
    
    private fun saveDailyGoal() {
        val goal = 1.0 + (binding.seekBarGoal.progress / 100.0) * 2.0
        saveGoalToDatabase(goal)
    }
    
    private fun saveGoalToDatabase(goal: Double) {
        lifecycleScope.launch {
            try {
                database.userDao().updateDailyGoal(currentUserId, goal)
                currentUser = database.userDao().getUserById(currentUserId)
                
                binding.tvCurrentGoal.text = "${String.format("%.1f", goal)}L"
                binding.cardCurrentGoal.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).withEndAction {
                    binding.cardCurrentGoal.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }.start()
                Toast.makeText(requireContext(), "Objectif mis à jour!", Toast.LENGTH_SHORT).show()
                
                // Add to goal history
                addGoalToHistory(goal)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addGoalToHistory(goal: Double) {
        val goalHistory = GoalHistoryItem(
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            goal = goal,
            achieved = false // Will be calculated later based on actual consumption
        )
        
        // In a real app, this would be stored in a separate table
        // For now, just update the UI
        updateGoalHistory()
    }
    
    private fun updateGoalHistory() {
        val historyList = generateGoalHistory()
        val adapter = GoalHistoryAdapter(historyList)
        binding.goalHistoryRecyclerView.adapter = adapter
    }
    
    private fun generateGoalHistory(): List<GoalHistoryItem> {
        val history = mutableListOf<GoalHistoryItem>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        for (i in 6 downTo 0) {
            val date = Date(Date().time - i * 24 * 60 * 60 * 1000L)
            val goal = currentUser?.dailyGoal ?: 2.0
            
            // Simulate random achievement (in real app, this would check actual consumption)
            val achieved = (0..1).random() == 1
            
            history.add(
                GoalHistoryItem(
                    date = dateFormat.format(date),
                    goal = goal,
                    achieved = achieved
                )
            )
        }
        
        return history
    }
    
    private fun updateGoalProgress() {
        lifecycleScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentGoal = currentUser?.dailyGoal ?: 2.0
                val actualConsumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, today)
                
                val progressPercentage = if (currentGoal > 0) {
                    ((actualConsumption / currentGoal) * 100).toInt()
                } else 0
                
                binding.tvGoalProgress.text = "$progressPercentage% de l'objectif atteint"
                
                // Update progress bar
                binding.progressGoal.max = (currentGoal * 100).toInt()
                binding.progressGoal.progress = (actualConsumption * 100).toInt()
                
                // Update progress color
                if (progressPercentage >= 100) {
                    binding.progressGoal.progressTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#7ED321") // Green
                    )
                } else {
                    binding.progressGoal.progressTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4A90E2") // Blue
                    )
                }
                
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateGoalProgress()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data classes for goal history
data class GoalHistoryItem(
    val date: String,
    val goal: Double,
    val achieved: Boolean
)

class GoalHistoryAdapter(private val items: List<GoalHistoryItem>) : androidx.recyclerview.widget.RecyclerView.Adapter<GoalHistoryAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_goal_date)
        val tvGoal: TextView = itemView.findViewById(R.id.tv_goal_amount)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_goal_status)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.example.exam.R.layout.item_goal_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date
        holder.tvGoal.text = "${String.format("%.1f", item.goal)}L"
        
        if (item.achieved) {
            holder.tvStatus.text = "✓ Atteint"
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#7ED321"))
        } else {
            holder.tvStatus.text = "✗ Non atteint"
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#E74C3C"))
        }
    }
    
    override fun getItemCount() = items.size
}