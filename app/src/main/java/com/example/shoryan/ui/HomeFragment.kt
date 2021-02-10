package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.RequestsFiltersContainer
import com.example.shoryan.databinding.FragmentHomeBinding
import com.example.shoryan.ui.recyclersAdapters.RequestsRecyclerAdapter
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.interfaces.FilterHolder
import com.example.shoryan.viewmodels.RequestsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class HomeFragment : Fragment(), RequestsRecyclerInteraction, FilterHolder {

    private lateinit var navController: NavController
    private val requestsRecyclerAdapter = RequestsRecyclerAdapter(this)

    // viewModels() connects the viewModel to the fragment, so, when the user navigates to another
    // tab using bottom navigation, the fragment is destroyed and therefore the attached viewModel
    // is destroyed as well. Therefore we used navGraphViewModels() to limit the scope of the
    // viewModel to the navComponent instead of individual fragment
    private val requestsViewModel: RequestsViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var requestsGettingJob: Job = Job()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewmodel = requestsViewModel
        binding.adapter = requestsRecyclerAdapter
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        // Getting ongoingRequests, pending request, my requests,  all in parallel
        updateUserPendingRequest()
        updateMyRequestsList()
        getOngoingRequests()
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun updateUserPendingRequest(){
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO){
                requestsViewModel.updateUserPendingRequest()
            }
        }
    }

    private fun updateMyRequestsList(){
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                requestsViewModel.updateMyRequestsList()
            }
        }
    }

    private fun getOngoingRequests(refresh: Boolean = false){
        requestsGettingJob.cancel()
        requestsGettingJob = viewLifecycleOwner.lifecycleScope.launch {
            requestsViewModel.getOngoingRequests(refresh).observe(viewLifecycleOwner, {
                binding.homeSwipeRefresh.isRefreshing = false
                requestsRecyclerAdapter.submitList(it)
            })
        }
    }

    override fun onResume(){
        super.onResume()
        setRecyclerViewScrollListener()
        setFilterListener()
        setPendingRequestCardListener()
        setMyRequestsCardListener()
    }

    private fun setRecyclerViewScrollListener(){
        binding.requestsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /* Only enable SwipeRefresh when the recyclerview's first element is visible (when
               the recyclerview reached top) */
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val firstPos = (recyclerView.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
                if (firstPos > 0) {
                    binding.homeSwipeRefresh.isEnabled = false
                } else {
                    binding.homeSwipeRefresh.isEnabled = true
                    if(recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING)
                        if(binding.homeSwipeRefresh.isRefreshing)
                            recyclerView.stopScroll()
                }
            }
        })
    }

    fun refreshRequests(){
        resetScrollingToTop()
        getOngoingRequests(true)
    }

    private fun setFilterListener(){
        binding.filter.setOnClickListener {
            resetScrollingToTop()
            FilterFragment.newInstance(requestsViewModel.restoreFilter())
                .show(childFragmentManager, "filterFragment")
        }
    }

    private fun resetScrollingToTop(){
        (binding.requestsRecycler.layoutManager as LinearLayoutManager).scrollToPosition(0)
        binding.homeAppBar.setExpanded(true)
    }

    private fun setPendingRequestCardListener(){
        binding.pendingRequestCard.setOnClickListener {
            val userPendingRequest = requestsViewModel.getUserPendingRequestId()
            if(userPendingRequest == null){
                showMessage("ليس لديك طلبات مُعلّقة")
            }
            else
                openDonationFragment(userPendingRequest)
        }
    }

    private fun setMyRequestsCardListener(){
        binding.myRequestsCard.setOnClickListener {
            val myRequests = requestsViewModel.getUserActiveRequests()
            if(myRequests.isEmpty())
                showMessage("ليس لديك طلبات حالياً")
            else
                openMyRequestsFragment(myRequests)
        }
    }

    private fun showMessage(message: String) =
        AndroidUtility.displaySnackbarMessage(binding.homeParentLayout, message, Snackbar.LENGTH_LONG)

    private fun openDonationFragment(requestId: String){
        val fragment = RequestDetailsFragment.newInstance(
            requestId,
            RequestDetailsFragment.REQUEST_FULFILLMENT_BINDING
        )
        fragment.show(childFragmentManager, "requestDetails")
    }

    private fun openMyRequestsFragment(myRequests: List<DonationRequest>) {
        val requests = bundleOf("requests" to myRequests)
        navController.navigate(R.id.action_home_to_myRequestsFragment, requests)
    }

    private fun openMyRequestDetailsFragment(requestId: String){
        val fragment =
            RequestDetailsFragment.newInstance(requestId, RequestDetailsFragment.MY_REQUEST_BINDING)
        fragment.show(childFragmentManager, "requestDetails")
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean){
        if(!isMyRequest){
            openDonationFragment(donationRequest.id)
        }
        else{
            openMyRequestDetailsFragment(donationRequest.id)
        }
    }

    override fun submitFilters(requestsFiltersContainer: RequestsFiltersContainer?) {
        requestsViewModel.storeFilter(requestsFiltersContainer)
        refreshRequests()
    }
}

