package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.R
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.AppbarBinding
import com.example.shoryan.databinding.FragmentMyRequestsBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.ui.recyclersAdapters.RequestsRecyclerAdapter
import com.example.shoryan.viewmodels.MyRequestsViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyRequestsFragment : Fragment(), RequestsRecyclerInteraction {

    private lateinit var navController: NavController
    private lateinit var requestsRecyclerAdapter: RequestsRecyclerAdapter

    private var _binding: FragmentMyRequestsBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null

    val tokensViewModel: TokensViewModel by viewModels()
    val viewModel: MyRequestsViewModel by viewModels()

   /* override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.myRequestsComponent().create().inject(this)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyRequestsBinding.inflate(inflater, container, false)
        toolbarBinding = binding.homeAppbar
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
        hideRequestsLoadingIndicator()
        instantiateNavController(view)
        setToolbarText(resources.getString(R.string.my_requests))
        binding.newRequestFAB.setOnClickListener { navController.navigate(R.id.action_myRequestsFragment_to_newRequest) }
    }

    override fun onResume() {
        super.onResume()
        getMyRequests()
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

    private fun getMyRequests(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getUserRequests().observe(viewLifecycleOwner){
                it.activeRequests?.let {
                    showRequestsDetails(it)
                }
                it.error?.let{
                    handleServerError(it.message)
                }
            }
        }
    }

    private fun handleServerError(error: ServerError){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            if(error == ServerError.JWT_EXPIRED){
                tokensViewModel.getNewAccessToken(requireContext())
            }
            else{
                error.doErrorAction(binding.rootLayout)
            }
        }
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean) {
        if(childFragmentManager.findFragmentByTag("requestDetails") != null){
            return
        }
        val fragment = RequestDetailsFragment.newInstance(
            donationRequest.id,
            RequestDetailsFragment.MY_REQUEST_BINDING
        )
        fragment.show(childFragmentManager, "requestDetails")
    }

    override fun onRequestCardDismissed() {
        getMyRequests()
    }
}