package com.example.exam.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exam.R
import com.example.exam.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        animateTopCard()
        setupRecyclerView()
        loadNotifications()
    }
    
    private fun animateTopCard() {
        binding.topCard.alpha = 0f
        binding.topCard.translationY = -100f
        binding.topCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    private fun setupRecyclerView() {
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun loadNotifications() {
        val notifications = listOf(
            NotificationItem("Rappel d'hydratation", "💧 Il est temps de boire de l'eau!", "Il y a 2 heures"),
            NotificationItem("Objectif atteint!", "🎉 Félicitations! Vous avez atteint votre objectif quotidien", "Il y a 5 heures"),
            NotificationItem("Rappel d'hydratation", "💦 N'oubliez pas de vous hydrater!", "Hier"),
            NotificationItem("Nouveau record!", "🏆 Vous avez bu 3L aujourd'hui!", "Hier"),
            NotificationItem("Rappel d'hydratation", "🌊 Votre corps a besoin d'eau!", "Il y a 2 jours")
        )
        
        binding.tvNotificationsCount.text = notifications.size.toString()
        binding.rvNotifications.adapter = NotificationsAdapter(notifications)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String
)

class NotificationsAdapter(private val notifications: List<NotificationItem>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_notification_title)
        val message: TextView = view.findViewById(R.id.tv_notification_message)
        val time: TextView = view.findViewById(R.id.tv_notification_time)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.time.text = notification.time
        
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_right)
        holder.itemView.startAnimation(animation)
    }
    
    override fun getItemCount() = notifications.size
}
