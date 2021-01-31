package com.example.sharyan.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sharyan.R
import kotlinx.android.synthetic.main.activity_my_request_details.*
import kotlinx.android.synthetic.main.appbar.*

class MyRequestDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_request_details)
        setToolbarText(resources.getString(R.string.my_request_details))

        if(intent != null){
            getDataFromIntent()
        }
    }

    private fun getDataFromIntent() {
        val bloodTypeText = intent.extras?.get("bloodType")
        bloodTypeResult.text = bloodTypeText.toString()

        val bagsFulfilledText = intent.extras?.get("bagsFulfilled")
        bagsFulfilledResult.text = bagsFulfilledText.toString()

        val locationTypeText = intent.extras?.get("donationLocation")
        locationResult.text = (locationTypeText).toString()

        val bagsCountText= intent.extras?.get("bagsRequired")
        bagsCountResult.text = bagsCountText.toString()

        val dateText= intent.extras?.get("date")
        dateResult.text = dateText.toString()
    }

    private fun setToolbarText(text: String) {
        toolbarText.text = text
    }
}