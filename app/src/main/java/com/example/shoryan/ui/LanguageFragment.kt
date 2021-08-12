package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.shoryan.LocaleHelper
import com.example.shoryan.databinding.FragmentLanguageBinding
import com.example.shoryan.interfaces.LocaleChangerHolder

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
            binding.arabicButton.isChecked -> LocaleHelper.LANGUAGE_AR
            binding.englishButton.isChecked -> LocaleHelper.LANGUAGE_EN
            else -> null
        }
    }
}