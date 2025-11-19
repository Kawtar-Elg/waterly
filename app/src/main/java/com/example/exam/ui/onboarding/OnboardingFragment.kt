package com.example.exam.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.exam.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    
    private var title: String = ""
    private var description: String = ""
    private var imageResId: Int = 0
    
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE_RES_ID = "image_res_id"
        
        fun newInstance(title: String, description: String, imageResId: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                    putInt(ARG_IMAGE_RES_ID, imageResId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE) ?: ""
            description = it.getString(ARG_DESCRIPTION) ?: ""
            imageResId = it.getInt(ARG_IMAGE_RES_ID, 0)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set content
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        
        if (imageResId != 0) {
            binding.ivImage.setImageResource(imageResId)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}