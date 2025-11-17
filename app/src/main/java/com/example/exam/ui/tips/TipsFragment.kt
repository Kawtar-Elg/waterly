package com.waterly.ui.tips

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.waterly.databinding.FragmentTipsBinding

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
    }
    
    private fun setupRecyclerView() {
        tipsAdapter = TipsAdapter { tip ->
            // Handle tip click
            showTipDetailDialog(tip)
        }
        
        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tipsAdapter
        }
    }
    
    private fun loadTipsData() {
        val tips = listOf(
            Tip("Buvez un verre d'eau avant chaque repas", "Recommandé par les nutritionnistes pour améliorer la digestion.", TipType.NUTRITION, R.drawable.ic_nutrition),
            Tip("Gardez une bouteille d'eau à portée de main", "Ayez toujours une bouteille d'eau près de vous pour ne jamais oublier de boire.", TipType.PRACTICE, R.drawable.ic_bottle),
            Tip("Utilisez des applications de rappel", "Programmez des rappels pour vous rappeler de boire de l'eau régulièrement.", TipType.TECHNOLOGY, R.drawable.ic_reminder),
            Tip("Ajoutez des fruits à votre eau", "Infusez votre eau avec des citron, concombre ou menthe pour plus de goût.", TipType.RECIPE, R.drawable.ic_fruit),
            Tip("Buvez de l'eau dès le réveil", "Commencez votre journée avec un verre d'eau pour hydrater votre corps après le sommeil.", TipType.MORNING, R.drawable.ic_sunrise),
            Tip("Remplacez les sodas par de l'eau", "Choisissez l'eau au lieu des boissons sucrées pour une meilleure hydratation.", TipType.SUBSTITUTION, R.drawable.ic_replace),
            Tip("Surveillez la couleur de votre urine", "Une urine jaune pâle indique une bonne hydratation, jaune foncé signifie que vous devez boire plus.", TipType.HEALTH, R.drawable.ic_health),
            Tip("Buvez plus par temps chaud", "Augmentez votre consommation d'eau quand il fait chaud ou quand vous faites de l'exercice.", TipType.WEATHER, R.drawable.ic_thermometer)
        )
        
        tipsAdapter.submitList(tips)
    }
    
    private fun setupListeners() {
        binding.btnShare.setOnClickListener {
            shareTips()
        }
        
        binding.btnWatchVideo.setOnClickListener {
            openHydrationVideo()
        }
        
        binding.btnReadArticles.setOnClickListener {
            openHydrationArticles()
        }
    }
    
    private fun showTipDetailDialog(tip: Tip) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(tip.title)
            .setMessage(tip.description)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun shareTips() {
        val shareText = "Découvrez ces conseils d'hydratation avec Waterly!\n\n" +
                "💧 Buvez un verre d'eau avant chaque repas\n" +
                "🍼 Gardez une bouteille d'eau à portée de main\n" +
                "📱 Utilisez des applications de rappel\n\n" +
                "Téléchargez Waterly pour rester hydraté!"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Partager les conseils"))
    }
    
    private fun openHydrationVideo() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=hydratation+bienfaits"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir la vidéo", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openHydrationArticles() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=hydratation+conseils+santé"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir les articles", Toast.LENGTH_SHORT).show()
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
    val iconResId: Int
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
        val tvType: TextView = itemView.findViewById(R.id.tv_tip_type)
        val ivIcon: androidx.appcompat.widget.AppCompatImageView = itemView.findViewById(R.id.iv_tip_icon)
        val cardTip: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.card_tip)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.tvTitle.text = tip.title
        holder.tvDescription.text = tip.description
        holder.tvType.text = tip.type.displayName
        holder.ivIcon.setImageResource(tip.iconResId)
        
        // Set card color based on tip type
        val cardColor = when (tip.type) {
            TipType.NUTRITION -> android.graphics.Color.parseColor("#FF9800")
            TipType.PRACTICE -> android.graphics.Color.parseColor("#2196F3")
            TipType.TECHNOLOGY -> android.graphics.Color.parseColor("#9C27B0")
            TipType.RECIPE -> android.graphics.Color.parseColor("#4CAF50")
            TipType.MORNING -> android.graphics.Color.parseColor("#FF5722")
            TipType.SUBSTITUTION -> android.graphics.Color.parseColor("#607D8B")
            TipType.HEALTH -> android.graphics.Color.parseColor("#E91E63")
            TipType.WEATHER -> android.graphics.Color.parseColor("#FFC107")
        }
        
        holder.cardTip.setCardBackgroundColor(cardColor)
        holder.cardTip.alpha = 0.9f
        
        holder.itemView.setOnClickListener { onTipClick(tip) }
    }
    
    override fun getItemCount() = tips.size
}