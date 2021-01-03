package com.example.sharyan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.CurrentAppUser
import kotlinx.android.synthetic.main.fragment_profile.*


class Profile : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
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
        profileGreeting.text = resources.getString(R.string.hello_user_greeting, CurrentAppUser.name?.firstName)
    }

    private fun displayStats(){
        pointsNumber.text = EnglishToArabicConverter().convertDigits(CurrentAppUser.points.toString())
        donationsNumber.text = EnglishToArabicConverter().convertDigits(CurrentAppUser.numberOfDonations.toString())
    }
}