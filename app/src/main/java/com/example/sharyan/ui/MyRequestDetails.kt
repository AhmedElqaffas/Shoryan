package com.example.sharyan.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sharyan.R
import kotlinx.android.synthetic.main.appbar.*

class MyRequestDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_request_details)
        setToolbarText(resources.getString(R.string.my_request_details))
    }

    private fun setToolbarText(text: String) {
        toolbarText.text = text
    }
}