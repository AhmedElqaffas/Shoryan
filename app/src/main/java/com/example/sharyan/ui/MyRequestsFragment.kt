package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.databinding.AppbarBinding
import com.example.sharyan.databinding.FragmentMyRequestsBinding
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import com.example.sharyan.recyclersAdapters.RequestsRecyclerInteraction
import com.google.android.material.snackbar.Snackbar

class MyRequestsFragment : Fragment(), RequestsRecyclerInteraction {

    private lateinit var navController: NavController
    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter
    private lateinit var requestsList: List<DonationRequest>

    private var _binding: FragmentMyRequestsBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestsList = requireArguments().get("requests") as List<DonationRequest>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyRequestsBinding.inflate(inflater, container, false)
        toolbarBinding = binding.homeAppbar
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbarBinding = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerViewAdapter()
        showRequestsDetails(requestsList)
        hideRequestsLoadingIndicator()
        instantiateNavController(view)
        setToolbarText(resources.getString(R.string.my_requests))
        binding.newRequestFAB.setOnClickListener { navController.navigate(R.id.action_myRequestsFragment_to_newRequest) }
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter(this)
        binding.myRequestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun showRequestsDetails(requestsList: List<DonationRequest>){
        requestsRecyclerAdapter.submitList(requestsList)
    }

    private fun hideRequestsLoadingIndicator(){
        binding.requestsShimmerContainer.stopShimmer()
        binding.requestsShimmerContainer.visibility = View.GONE
        binding.myRequestsRecycler.visibility = View.VISIBLE
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setToolbarText(text: String){
        binding.homeAppbar.toolbarText.text = text
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean) {
        val fragment = RequestFulfillmentFragment.newInstance(donationRequest.id,
            RequestFulfillmentFragment.MY_REQUEST_BINDING)
        fragment.show(childFragmentManager, "requestDetails")
    }
}