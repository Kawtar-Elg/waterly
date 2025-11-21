package com.example.exam.ui.tips

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.exam.R
import com.example.exam.databinding.FragmentTipsBinding

class TipsFragment : Fragment() {
    
    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tipsAdapter: TipsAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadTipsData()
        setupListeners()
        animateRecyclerView()
    }
    
    private fun animateRecyclerView() {
        binding.rvTips.alpha = 0f
        binding.rvTips.translationY = 50f
        binding.rvTips.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
    }
    
    private fun setupRecyclerView() {
        tipsAdapter = TipsAdapter { tip ->
            openVideo(tip.videoUrl)
        }
        
        binding.rvTips.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = tipsAdapter
        }
    }
    
    private fun loadTipsData() {
        val tips = listOf(
            Tip("Importance de l'hydratation - conseils", "Conseils pratiques à suivre pour rester hydraté", TipType.NUTRITION, R.drawable.youtubevid1, "https://www.youtube.com/watch?v=9iMGFqMmUFs"),
            Tip("Rester hydraté pour votre santé", "Conseils sur l'hydratation pour votre santé", TipType.HEALTH, R.drawable.youtubevid2, "https://www.youtube.com/watch?v=9iMGFqMmUFs"),
            Tip("Importance de l'hydratation - conseils", "Conseils pratiques à suivre pour rester hydraté", TipType.PRACTICE, R.drawable.youtubevid3, "https://www.youtube.com/watch?v=9iMGFqMmUFs"),
            Tip("Rester hydraté pour votre santé", "Conseils sur l'hydratation pour votre santé", TipType.WEATHER, R.drawable.youtubevid4, "https://www.youtube.com/watch?v=9iMGFqMmUFs")
        )
        
        tipsAdapter.submitList(tips)
    }
    
    private fun setupListeners() {
        // Tab buttons handled by parent fragment
    }
    
    private fun openVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir la vidéo", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data classes
data class Tip(
    val title: String,
    val description: String,
    val type: TipType,
    val iconResId: Int,
    val videoUrl: String
)

enum class TipType(val displayName: String) {
    NUTRITION("Nutrition"),
    PRACTICE("Pratique"),
    TECHNOLOGY("Technologie"),
    RECIPE("Recette"),
    MORNING("Matin"),
    SUBSTITUTION("Remplacement"),
    HEALTH("Santé"),
    WEATHER("Météo")
}

class TipsAdapter(private val onTipClick: (Tip) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<TipsAdapter.TipViewHolder>() {
    
    private var tips = emptyList<Tip>()
    
    fun submitList(newTips: List<Tip>) {
        tips = newTips
        notifyDataSetChanged()
    }
    
    class TipViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_tip_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_tip_description)
        val ivThumbnail: android.widget.ImageView = itemView.findViewById(R.id.iv_video_thumbnail)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.example.exam.R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.tvTitle.text = tip.title
        holder.tvDescription.text = tip.description
        holder.ivThumbnail.setImageResource(tip.iconResId)
        
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 50f
        holder.itemView.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay((position * 100).toLong()).start()
        
        holder.itemView.setOnClickListener { 
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
            onTipClick(tip)
        }
    }
    
    override fun getItemCount() = tips.size
}