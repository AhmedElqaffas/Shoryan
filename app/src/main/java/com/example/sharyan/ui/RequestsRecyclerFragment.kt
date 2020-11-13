package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_requests_recycler.*

class RequestsRecyclerFragment : Fragment() {

    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View?{
        return inflater.inflate(R.layout.fragment_requests_recycler, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRecyclerViewAdapter()
        setupDummyData()
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter()
        requestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun setupDummyData(){
        requestsRecyclerAdapter.addRequests(
            listOf(
                DonationRequest("AB", "عبد الغفور البرعي", "مدينة نصر"),
                DonationRequest("B+", "مدحت قلابيظو", "المقطم"),
                DonationRequest("B", "الفارس الشجاع", "المقطم"),
                DonationRequest("A+", "وردة البستان", "مصر الجديدة"),
                DonationRequest("O-", "صديق الفلاح", "6 اكتوبر"),
                DonationRequest("B+", "الفلاح", "المعادي"),

            )

        )
    }


}