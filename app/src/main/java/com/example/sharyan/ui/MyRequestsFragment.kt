package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.fragment_my_requests.*

class MyRequestsFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        setToolbarText(resources.getString(R.string.my_requests))
        newRequestFAB.setOnClickListener { navController.navigate(R.id.action_myRequestsFragment_to_newRequest) }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }


}