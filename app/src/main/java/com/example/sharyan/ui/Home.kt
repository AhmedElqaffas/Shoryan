package com.example.sharyan.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.sharyan.R
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class Home : Fragment(){

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRecyclerViewAdapter()
        getOngoingRequests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestsGettingJob?.cancel()
    }

    override fun onResume(){
        super.onResume()
        setToolbarText(resources.getString(R.string.home))
        setRecyclerViewScrollListener()
        setRecyclerViewTouchListener()
        setSwipeRefreshListener()
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun setRecyclerViewScrollListener(){
        requestsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /*
             * When the recyclerview is not being dragged, enable the SwipeRefresh
             * This method, accompanied with the setRecyclerViewTouchListener(), make sure
             * the pull to refresh functionality only works outside the recyclerview
             */
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                   homeSwipeRefresh.isEnabled = true
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setRecyclerViewTouchListener(){
        /*
         * To avoid "SwipeRefresh" interfering with recyclerview scrolling, we disable the SwipeRefresh
         * when the recyclerview is touched.
         */
        requestsRecycler.setOnTouchListener { _, _ ->
            homeSwipeRefresh.isEnabled = false
            false
        }

    }

    private fun setSwipeRefreshListener(){
        homeSwipeRefresh.setOnRefreshListener{
            getOngoingRequests(true)
        }
    }

    private fun initializeRecyclerViewAdapter(){
        requestsRecyclerAdapter = RequestsRecyclerAdapter()
        requestsRecycler.adapter = requestsRecyclerAdapter
    }

    private fun getOngoingRequests(refresh: Boolean = false){
        requestsBeingLoaded()
        requestsGettingJob = CoroutineScope(Dispatchers.Main).launch {
            requestsViewModel.getOngoingRequests(refresh).observe(viewLifecycleOwner, {
                homeSwipeRefresh.isRefreshing = false
                if(it.isNotEmpty()){
                    requestsLoaded()
                }
                requestsRecyclerAdapter.submitList(it)
            })
        }
    }

    private fun requestsBeingLoaded(){
        requestsGettingJob?.cancel()
        requestsShimmerContainer.startShimmer()
        requestsShimmerContainer.visibility = View.VISIBLE
        requestsRecycler.visibility = View.GONE
    }

    private fun requestsLoaded(){
        requestsShimmerContainer.stopShimmer()
        requestsShimmerContainer.visibility = View.GONE
        requestsRecycler.visibility = View.VISIBLE
    }
}