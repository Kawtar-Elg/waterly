package com.example.exam.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exam.WaterlyApp
import com.example.exam.data.database.AppDatabase
import com.example.exam.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            if (_binding != null) {
                binding.ivProfile.setImageURI(it)
                binding.ivProfile.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                Toast.makeText(requireContext(), "Image sélectionnée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImage.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("waterly_prefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)
        database = (requireActivity().application as WaterlyApp).database

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val user = database.userDao().getUserById(currentUserId)
                user?.let {
                    if (isAdded && _binding != null) {
                        binding.etName.setText(it.username)
                        binding.etEmail.setText(it.email)
                        binding.etBirthday.setText(it.birthday)
                    }
                }
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnCamera.setOnClickListener {
            openGallery()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImage.launch("image/*")
            }
            else -> {
                requestPermission.launch(permission)
            }
        }
    }

    private fun saveProfile() {
        if (!isAdded || _binding == null) return

        val name = binding.etName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = database.userDao().getUserById(currentUserId)
                if (user != null) {
                    val updatedUser = user.copy(
                        username = name,
                        email = email,
                        password = if (password.isNotEmpty()) password else user.password
                    )
                    database.userDao().updateUser(updatedUser)
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Profil mis à jour!", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
