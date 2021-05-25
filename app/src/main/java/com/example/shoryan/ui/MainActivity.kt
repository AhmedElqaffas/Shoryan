package com.example.shoryan.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.shoryan.LocaleHelper
import com.example.shoryan.R
import com.example.shoryan.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationComponent()
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(LocaleHelper.onAttach(newBase, Locale.getDefault().language))
        }
    }

    private fun setNavigationComponent(){
        val navigationController: NavController = findNavController(R.id.fragmentsContainer)
        binding.mainBottomNavigationView.setupWithNavController(navigationController)
    }
}