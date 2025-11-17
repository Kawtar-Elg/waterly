package com.waterly.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.waterly.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    
    private var title: String = ""
    private var description: String = ""
    private var imageResId: Int = 0
    private var backgroundColor: Int = 0
    
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE_RES_ID = "image_res_id"
        private const val ARG_BACKGROUND_COLOR = "background_color"
        
        fun newInstance(title: String, description: String, imageResId: Int, backgroundColor: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                    putInt(ARG_IMAGE_RES_ID, imageResId)
                    putInt(ARG_BACKGROUND_COLOR, backgroundColor)
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
            backgroundColor = it.getInt(ARG_BACKGROUND_COLOR, 0)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set background color
        if (backgroundColor != 0) {
            binding.root.setBackgroundResource(backgroundColor)
        }
        
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