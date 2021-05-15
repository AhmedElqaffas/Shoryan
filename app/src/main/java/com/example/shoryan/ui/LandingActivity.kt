package com.example.shoryan.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shoryan.LocaleHelper
import com.example.shoryan.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class LandingActivity : AppCompatActivity(){

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(LocaleHelper.onAttach(newBase, Locale.getDefault().language))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
    }

}
