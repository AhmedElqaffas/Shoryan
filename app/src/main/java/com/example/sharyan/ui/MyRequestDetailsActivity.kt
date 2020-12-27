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

        val govText = intent.extras?.get("gov")
        govResult.text = govText.toString()

        val cityTypeText = intent.extras?.get("city")
        cityResult.text = (cityTypeText).toString()

        val bagsCountText= intent.extras?.get("bloodBagsCount")
        bagsCountResult.text = bagsCountText.toString()
    }

    private fun setToolbarText(text: String) {
        toolbarText.text = text
    }
}