package com.example.sharyan.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sharyan.R
import com.example.sharyan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationComponent()
    }

    private fun setNavigationComponent(){
        val navigationController: NavController = findNavController(R.id.fragmentsContainer)
        binding.mainBottomNavigationView.setupWithNavController(navigationController)
    }
}