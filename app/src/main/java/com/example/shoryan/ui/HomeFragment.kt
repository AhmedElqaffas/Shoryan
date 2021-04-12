package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentHomeBinding
import com.example.shoryan.di.MyApplication
import com.example.shoryan.ui.recyclersAdapters.RequestsRecyclerAdapter
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.interfaces.FilterHolder
import com.example.shoryan.viewmodels.RequestsViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import javax.inject.Inject

class HomeFragment : Fragment(), RequestsRecyclerInteraction, FilterHolder {

    private lateinit var navController: NavController
    private val requestsRecyclerAdapter = RequestsRecyclerAdapter(this)
    @Inject
    lateinit var tokensViewModel: TokensViewModel

    // viewModels() connects the viewModel to the fragment, so, when the user navigates to another
    // tab using bottom navigation, the fragment is destroyed and therefore the attached viewModel
    // is destroyed as well. Therefore we used navGraphViewModels() to limit the scope of the
    // viewModel to the navComponent instead of individual fragment
    private val requestsViewModel: RequestsViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var requestsGettingJob: Job = Job()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.homeComponent().create().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewmodel = requestsViewModel
        binding.adapter = requestsRecyclerAdapter
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestsGettingJob.cancel()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        // Getting ongoingRequests, pending request, and user data, in parallel
        updateUserPendingRequest()
        getOngoingRequests()
        getUserProfileData()
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

    private fun getOngoingRequests(){
        requestsGettingJob.cancel()
        requestsGettingJob = viewLifecycleOwner.lifecycleScope.launch {
            requestsViewModel.getOngoingRequests().observe(viewLifecycleOwner, {
                binding.homeSwipeRefresh.isRefreshing = false
                println(it.toString())
                it.requests?.let{
                    requestsRecyclerAdapter.submitList(it)
                }
                it.error?.let{
                    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                        handleError(it.message)
                    }
                }
            })
        }
    }

    private fun handleError(error: ServerError){
        if(error == ServerError.JWT_EXPIRED){
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let{
                    forceLogOut()
                }
            }
        }
        else{
            error.doErrorAction(binding.root)
        }
    }

    private fun forceLogOut(){
        Toast.makeText(requireContext(), resources.getString(R.string.re_login), Toast.LENGTH_LONG).show()
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun getUserProfileData(){
       lifecycleScope.launch{
           requestsViewModel.getProfileData()
       }
    }

    override fun onResume(){
        super.onResume()
        setRecyclerViewScrollListener()
        setFilterListener()
        setPendingRequestCardListener()
        setMyRequestsCardListener()
        setRewardsCardListener()
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
        getOngoingRequests()
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
                showMessage(resources.getString(R.string.no_pending_requests))
            }
            else
                openDonationFragment(userPendingRequest)
        }
    }

    private fun setMyRequestsCardListener(){
        binding.myRequestsCard.setOnClickListener {
            openMyRequestsFragment()
        }
    }

    private fun showMessage(message: String) =
        AndroidUtility.displaySnackbarMessage(binding.rootLayout, message, Snackbar.LENGTH_LONG)

    private fun openDonationFragment(requestId: String){
        val fragment = RequestDetailsFragment.newInstance(
            requestId,
            RequestDetailsFragment.REQUEST_FULFILLMENT_BINDING
        )
        fragment.show(childFragmentManager, "requestDetails")
    }

    private fun openMyRequestsFragment() {
        navController.navigate(R.id.action_home_to_myRequestsFragment)
    }

    private fun openMyRequestDetailsFragment(requestId: String){
        val fragment =
            RequestDetailsFragment.newInstance(requestId, RequestDetailsFragment.MY_REQUEST_BINDING)
        fragment.show(childFragmentManager, "requestDetails")
    }

    private fun setRewardsCardListener(){
        binding.redeemRewardsCard.setOnClickListener {
            openRewardsFragment()
        }
    }

    private fun openRewardsFragment(){
        navController.navigate(R.id.action_home_to_rewardsFragment)
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean){
        if(childFragmentManager.findFragmentByTag("requestDetails") != null){
            return
        }
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

