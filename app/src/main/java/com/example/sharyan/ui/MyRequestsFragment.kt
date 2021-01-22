package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import com.example.sharyan.recyclersAdapters.RequestsRecyclerInteraction
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_my_requests.*

class MyRequestsFragment : Fragment(), RequestsRecyclerInteraction {

    private lateinit var navController: NavController
    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter
    private lateinit var requestsList: List<DonationRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestsList = requireArguments().get("requestsIDs") as List<DonationRequest>

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerViewAdapter()
        showRequestsDetails(requestsList)
        hideRequestsLoadingIndicator()
        instantiateNavController(view)
        setToolbarText(resources.getString(R.string.my_requests))
        newRequestFAB.setOnClickListener { navController.navigate(R.id.action_myRequestsFragment_to_newRequest) }
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter(this)
        myRequestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun showRequestsDetails(requestsList: List<DonationRequest>){
        requestsRecyclerAdapter.submitList(requestsList)
    }

    private fun hideRequestsLoadingIndicator(){
        requestsShimmerContainer.stopShimmer()
        requestsShimmerContainer.visibility = View.GONE
        myRequestsRecycler.visibility = View.VISIBLE
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest) {
        Snackbar.make(myRequestsLayout, "Should Open request details screen", Snackbar.LENGTH_LONG)
            .setAction("حسناً") {
                // By default, the snackbar will be dismissed
            }
            .setActionTextColor(resources.getColor(R.color.colorAccent))
            .show()
    }
}