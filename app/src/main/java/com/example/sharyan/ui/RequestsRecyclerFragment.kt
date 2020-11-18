package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_requests_recycler.*

class RequestsRecyclerFragment : Fragment() {

    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter

    private val requestsViewModel: RequestsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View?{
        return inflater.inflate(R.layout.fragment_requests_recycler, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRecyclerViewAdapter()
        getOngoingRequests()
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