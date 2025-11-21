package com.example.exam.ui.dashboard

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exam.R
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.data.entity.WaterConsumption
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardContentFragment : Fragment() {
    
    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard_content, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        
        database = (requireActivity().application as WaterlyApp).database
        
        animateViews(view)
        setupViews(view)
        loadTodayData(view)
        setupFloatingActionButton(view)
    }
    
    private fun animateViews(view: View) {
        val cardBottle = view.findViewById<androidx.cardview.widget.CardView>(R.id.card_bottle)
        val cardStats = view.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.card_stats)
        val fabAddWater = view.findViewById<android.widget.ImageView>(R.id.fab_add_water)
        
        cardBottle.alpha = 0f
        cardStats.alpha = 0f
        fabAddWater.alpha = 0f
        
        cardBottle.animate().alpha(1f).setDuration(500).setStartDelay(100).start()
        cardStats.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start()
        fabAddWater.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(400).start()
    }
    
    private fun setupViews(view: View) {
        val lottieBottle = view.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottie_bottle)
        
        try {
            lottieBottle.setAnimation(R.raw.animationbottle)
            lottieBottle.repeatCount = ValueAnimator.INFINITE
            lottieBottle.loop(true)
            lottieBottle.playAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadTodayData(view: View) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val totalConsumption = database.waterConsumptionDao().getTotalConsumptionByDate(currentUserId, today)
                val goalLiters = 2.0
                val percentage = ((totalConsumption / goalLiters) * 100).toInt().coerceAtMost(100)
                
                val tvDailyGoal = view.findViewById<android.widget.TextView>(R.id.tv_daily_goal)
                val tvConsumptionAmount = view.findViewById<android.widget.TextView>(R.id.tv_consumption_amount)
                val waterCircleView = view.findViewById<WaterCircleView>(R.id.water_circle_view)
                val lottieBottle = view.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottie_bottle)
                val cardAchievement = view.findViewById<androidx.cardview.widget.CardView>(R.id.card_achievement)
                
                tvDailyGoal.text = "${String.format("%.1f", totalConsumption)} Litres"
                tvConsumptionAmount.text = "${String.format("%.1f", totalConsumption)}L $percentage%"
                waterCircleView.setPercentage(percentage)
                
                val progress = percentage / 100f
                lottieBottle.progress = progress
                
                if (percentage >= 100) {
                    cardAchievement.visibility = View.VISIBLE
                    cardAchievement.alpha = 0f
                    cardAchievement.scaleX = 0.8f
                    cardAchievement.scaleY = 0.8f
                    cardAchievement.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setInterpolator(android.view.animation.BounceInterpolator()).start()
                } else {
                    cardAchievement.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupFloatingActionButton(view: View) {
        val fabAddWater = view.findViewById<android.widget.ImageView>(R.id.fab_add_water)
        var dX = 0f
        var dY = 0f
        var initialX = 0f
        var initialY = 0f
        
        fabAddWater.scaleX = 0f
        fabAddWater.scaleY = 0f

        fabAddWater.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    initialX = event.rawX
                    initialY = event.rawY
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    val deltaX = kotlin.math.abs(event.rawX - initialX)
                    val deltaY = kotlin.math.abs(event.rawY - initialY)
                    if (deltaX < 10 && deltaY < 10) {
                        showAddWaterDialog()
                    }
                    v.performClick()
                    true
                }
                else -> false
            }
        }
        
        fabAddWater.setOnClickListener {}
    }
    
    private fun showAddWaterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_water, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.alpha = 0f
        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        
        val cards = listOf(
            dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_250ml),
            dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_500ml),
            dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_750ml),
            dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_1l)
        )
        
        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()
                val amount = when(index) {
                    0 -> 0.25f
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1.0f
                }
                addWaterConsumption(amount)
                dialog.dismiss()
            }
        }
        
        dialog.show()
        dialogView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).setInterpolator(android.view.animation.OvershootInterpolator()).start()
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
                    view?.let { loadTodayData(it) }
                }
                
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}