package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.example.sharyan.R
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Home : Fragment(){

    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter

    // viewModels() connects the viewModel to the fragment, so, when the user navigates to another
    // tab using bottom navigation, the fragment is destroyed and therefore the attached viewModel
    // is destroyed as well. Therefore we used navGraphViewModels() to limit the scope of the
    // viewModel to the navComponent instead of individual fragment
    private val requestsViewModel: RequestsViewModel by navGraphViewModels(R.id.main_nav_graph)

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
        CoroutineScope(Dispatchers.Main).launch {
            requestsViewModel.getOngoingRequests().observe(viewLifecycleOwner,  {
                requestsRecyclerAdapter.submitList(it)

            })
        }
    }
}