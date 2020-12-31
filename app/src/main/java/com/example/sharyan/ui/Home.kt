package com.example.sharyan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.RequestsFilter
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import com.example.sharyan.recyclersAdapters.RequestsRecyclerInteraction
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Home : Fragment(), RequestsRecyclerInteraction, FilterHolder{

    private lateinit var navController: NavController
    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter

    // viewModels() connects the viewModel to the fragment, so, when the user navigates to another
    // tab using bottom navigation, the fragment is destroyed and therefore the attached viewModel
    // is destroyed as well. Therefore we used navGraphViewModels() to limit the scope of the
    // viewModel to the navComponent instead of individual fragment
    private val requestsViewModel: RequestsViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var requestsGettingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRecyclerViewAdapter()
        // Getting ongoingRequests, pending request, my requests,  all in parallel
        updateUserPendingRequest()
        updateMyRequestsList()
        getOngoingRequests()

    }

    override fun onResume(){
        super.onResume()

        setToolbarText(resources.getString(R.string.home))
        setRecyclerViewScrollListener()
        setSwipeRefreshListener()
        setFilterListener()
        setPendingRequestCardListener()
        setMyRequestsCardListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestsGettingJob?.cancel()
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun setRecyclerViewScrollListener(){
        requestsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /* Only enable SwipeRefresh when the recyclerview's first element is visible (when
               the recyclerview reached top) */

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val firstPos = (recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition()
                    if (firstPos > 0) {
                        homeSwipeRefresh.isEnabled = false
                    } else {
                        homeSwipeRefresh.isEnabled = true
                        if(recyclerView.scrollState == 1)
                            if(homeSwipeRefresh.isRefreshing)
                                recyclerView.stopScroll()
                    }
            }
        })
    }

    private fun setSwipeRefreshListener(){
        homeSwipeRefresh.setOnRefreshListener{
            getOngoingRequests(true)
        }
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter(this)
        requestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun updateUserPendingRequest(){
        CoroutineScope(Dispatchers.IO).launch {
            requestsViewModel.updateUserPendingRequest()
        }
    }

    private fun updateMyRequestsList(){
        CoroutineScope(Dispatchers.IO).launch {
            requestsViewModel.updateMyRequestsList()
        }
    }

    private fun getOngoingRequests(refresh: Boolean = false){
        showRequestsLoadingIndicator()
        requestsGettingJob = CoroutineScope(Dispatchers.Main).launch {
            requestsViewModel.getOngoingRequests(refresh).observe(viewLifecycleOwner, {
                homeSwipeRefresh.isRefreshing = false
                hideRequestsLoadingIndicator()
                requestsRecyclerAdapter.submitList(it)
            })
        }
    }

    private fun showRequestsLoadingIndicator(){
        requestsGettingJob?.cancel()
        requestsShimmerContainer.startShimmer()
        requestsShimmerContainer.visibility = View.VISIBLE
        requestsRecycler.visibility = View.GONE
    }

    private fun hideRequestsLoadingIndicator(){
        requestsShimmerContainer.stopShimmer()
        requestsShimmerContainer.visibility = View.GONE
        requestsRecycler.visibility = View.VISIBLE
    }

    private fun setFilterListener(){
        filter.setOnClickListener {
            resetScrollingToTop()

            FilterFragment(this, requestsViewModel.restoreFilter())
                .show(childFragmentManager, "filterFragment")
        }
    }

    private fun resetScrollingToTop(){
        (requestsRecycler.layoutManager as LinearLayoutManager).scrollToPosition(0)
        homeAppBar.setExpanded(true)
    }

    private fun setMyRequestsCardListener(){
        myRequestsCard.setOnClickListener {
            val myRequests = requestsViewModel.getUserActiveRequests()
            if(myRequests.isEmpty()) showSnackBar("ليس لديك طلبات حالياً") else openMyRequestsFragment()
        }
    }

    private fun setPendingRequestCardListener(){
        pendingRequestCard.setOnClickListener {
            val userPendingRequest = requestsViewModel.getUserPendingRequest()
            if(userPendingRequest == null){
                showSnackBar("ليس لديك طلبات مُعلّقة")
            }
            else{
                openDonationFragment(userPendingRequest)
            }
        }
    }

    private fun showSnackBar(message: String){
        Snackbar.make(homeParentLayout, message, Snackbar.LENGTH_LONG)
            .setAction("حسناً") {
                // By default, the snackbar will be dismissed
            }
            .setActionTextColor(resources.getColor(R.color.colorAccent))
            .show()
    }

    private fun openDonationFragment(donationRequest: DonationRequest){
        val fragment = RequestFulfillmentFragment.newInstance(donationRequest)
        fragment.show(childFragmentManager, "requestDetails")
    }

    private fun openMyRequestsFragment(){
        navController.navigate(R.id.action_home_to_myRequestsFragment)
    }

    override fun onItemClicked(donationRequest: DonationRequest) {
        openDonationFragment(donationRequest)
    }

    override fun submitFilters(requestsFilter: RequestsFilter?) {
        requestsViewModel.storeFilter(requestsFilter)
        getOngoingRequests( true)
    }
}

interface FilterHolder{
    fun submitFilters(requestsFilter: RequestsFilter?) {
    }
}

