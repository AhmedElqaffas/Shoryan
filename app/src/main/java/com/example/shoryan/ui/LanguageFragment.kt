package com.example.shoryan.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.LocaleHelper
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentLanguageBinding
import com.example.shoryan.databinding.FragmentSplashScreenBinding
import com.example.shoryan.interfaces.LocaleChangerHolder
import java.util.*

class LanguageFragment : DialogFragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.changeLanguageButton.setOnClickListener {changeLocale()}
        binding.arabicButton.setOnClickListener { binding.englishButton.isChecked = false }
        binding.englishButton.setOnClickListener { binding.arabicButton.isChecked = false }
    }

    private fun changeLocale(){
        getChosenLanguage()?.let{
            (parentFragment as LocaleChangerHolder).onLocaleChanged(it)
            dismiss()
        }
    }

    private fun getChosenLanguage(): String?{
        return when {
            binding.arabicButton.isChecked -> "ar"
            binding.englishButton.isChecked -> "en"
            else -> null
        }
    }
}