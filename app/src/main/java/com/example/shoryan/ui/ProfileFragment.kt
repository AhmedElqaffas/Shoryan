package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.R
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.databinding.FragmentProfileBinding
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.viewmodels.ProfileViewModel


class ProfileFragment : Fragment() {

    private val bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
            .getRetrofitClient()
            .create(RetrofitBloodDonationInterface::class.java)

    private val profileViewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewmodel = profileViewModel
        binding.englishArabicConverter = EnglishToArabicConverter()
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserData()
    }

    private fun fetchUserData(){
        profileViewModel.getProfileData(bloodDonationAPI)
    }
}