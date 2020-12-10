package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.sharyan.R
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_home.*

class Home : Fragment() {

    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter
    private val requestsViewModel: RequestsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRecyclerViewAdapter()
        getOngoingRequests()
    }

    override fun onResume() {
        super.onResume()
        setToolbarText(resources.getString(R.string.home))
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter()
        requestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun getOngoingRequests(){
        requestsViewModel.getOngoingRequests().observe(viewLifecycleOwner,  {
            requestsRecyclerAdapter.addRequests(it)
        })
    }
}