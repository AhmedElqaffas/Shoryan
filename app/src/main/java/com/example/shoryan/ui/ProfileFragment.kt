package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.R
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayInformation()
    }

    private fun displayInformation(){
        displayGreeting()
        displayStats()
    }

    private fun displayGreeting(){
        binding.profileGreeting.text = resources.getString(R.string.hello_user_greeting, CurrentAppUser.name?.firstName)
    }

    private fun displayStats(){
        with(binding){
            pointsNumber.text = EnglishToArabicConverter().convertDigits(CurrentAppUser.points.toString())
            donationsNumber.text = EnglishToArabicConverter().convertDigits(CurrentAppUser.numberOfDonations.toString())
        }
    }
}