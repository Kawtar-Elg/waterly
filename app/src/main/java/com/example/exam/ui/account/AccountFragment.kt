package com.waterly.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exam.WaterlyApp
import com.waterly.data.database.AppDatabase
import com.example.exam.data.entity.User
import com.waterly.databinding.FragmentAccountBinding
import com.waterly.ui.auth.AuthActivity
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private var currentUser: User? = null
    private var currentUserId: Long = 0
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get user ID from preferences
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        
        database = (requireActivity().application as WaterlyApp).database
        
        loadUserData()
        setupListeners()
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                currentUser = database.userDao().getUserById(currentUserId)
                displayUserData()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors du chargement des données utilisateur", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun displayUserData() {
        currentUser?.let { user ->
            binding.tvUsername.text = user.username
            binding.tvEmail.text = user.email
            binding.tvBirthday.text = user.birthday
            binding.tvDailyGoal.text = "${String.format("%.1f", user.dailyGoal)}L"
            
            // Set language
            val languageName = when (user.language) {
                "fr" -> "Français"
                "en" -> "English"
                "ar" -> "العربية"
                else -> "Français"
            }
            binding.tvLanguage.text = languageName
        }
    }
    
    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        binding.btnNotifications.setOnClickListener {
            showNotificationsSettings()
        }
        
        binding.btnPrivacy.setOnClickListener {
            showPrivacySettings()
        }
        
        binding.btnLanguage.setOnClickListener {
            showLanguageSelection()
        }
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
        
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    
    private fun showEditProfileDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier le profil")
            .setMessage("Fonctionnalité en cours de développement")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showChangePasswordDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "Nouveau mot de passe"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Changer le mot de passe")
            .setView(input)
            .setPositiveButton("Confirmer") { dialog, _ ->
                val newPassword = input.text.toString()
                if (newPassword.isNotEmpty()) {
                    updatePassword(newPassword)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    val updatedUser = user.copy(password = newPassword)
                    database.userDao().updateUser(updatedUser)
                    Toast.makeText(requireContext(), "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showNotificationsSettings() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Paramètres de notification")
            .setMessage("Les rappels d'eau sont actifs tous les 2 heures")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showPrivacySettings() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confidentialité")
            .setMessage("Vos données sont stockées localement sur votre appareil")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showLanguageSelection() {
        val languages = arrayOf("Français", "English", "العربية")
        val currentLanguage = currentUser?.language ?: "fr"
        val currentIndex = when (currentLanguage) {
            "fr" -> 0
            "en" -> 1
            "ar" -> 2
            else -> 0
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Choisir la langue")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val newLanguage = when (which) {
                    0 -> "fr"
                    1 -> "en"
                    2 -> "ar"
                    else -> "fr"
                }
                updateLanguage(newLanguage)
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun updateLanguage(language: String) {
        lifecycleScope.launch {
            try {
                database.userDao().updateLanguage(currentUserId, language)
                currentUser = database.userDao().getUserById(currentUserId)
                
                // Update shared preferences
                val prefs = requireActivity().getSharedPreferences("waterly_prefs", MODE_PRIVATE)
                prefs.edit().putString("language", language).apply()
                
                Toast.makeText(requireContext(), "Langue changée", Toast.LENGTH_SHORT).show()
                
                // Update UI
                displayUserData()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors du changement de langue", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Se déconnecter") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun performLogout() {
        // Clear user preferences
        val prefs = requireActivity().getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        // Navigate to auth activity
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le compte")
            .setMessage("Cette action est irréversible. Toutes vos données seront supprimées.")
            .setPositiveButton("Supprimer") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun performDeleteAccount() {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    database.userDao().deleteUser(user)
                }
                
                Toast.makeText(requireContext(), "Compte supprimé", Toast.LENGTH_SHORT).show()
                
                // Navigate to auth activity
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors de la suppression du compte", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}