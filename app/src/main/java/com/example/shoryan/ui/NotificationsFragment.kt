package com.example.shoryan.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.R
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.AppbarBinding
import com.example.shoryan.databinding.FragmentNotificationsBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.ui.recyclersAdapters.NotificationsRecyclerAdapter
import com.example.shoryan.viewmodels.NotificationsViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment(), RequestsRecyclerInteraction {

    private lateinit var notificationsRecyclerAdapter: NotificationsRecyclerAdapter
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    val tokensViewModel: TokensViewModel by viewModels()

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        toolbarBinding = binding.notificationsAppbar
        binding.viewModel = notificationsViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        setToolbarText(resources.getString(R.string.notifications))
        initializeRecyclerViewAdapter()
        showNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        toolbarBinding = null
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setToolbarText(text: String){
        toolbarBinding!!.toolbarText.text = text
    }

    private fun initializeRecyclerViewAdapter(){
        notificationsRecyclerAdapter = NotificationsRecyclerAdapter(this, notificationsViewModel, viewLifecycleOwner)
        binding.notificationsRecycler.adapter = notificationsRecyclerAdapter
    }

    private fun showNotifications(){
        viewLifecycleOwner.lifecycleScope.launch {
            notificationsViewModel.getNotifications().observe(viewLifecycleOwner){
                it?.notifications?.let { notificationsList ->
                    notificationsRecyclerAdapter.submitList(notificationsList)
                }

                it?.error?.let{ error ->
                    handleServerError(error.message)
                }
            }
        }
    }

    private fun handleServerError(error: ServerError){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            if(error == ServerError.JWT_EXPIRED){
                tokensViewModel.getNewAccessToken(requireContext())
            }
            // Connection Error is already handled by showing a custom view
            else if(error != ServerError.CONNECTION_ERROR){
                error.doErrorAction(binding.rootLayout)
            }
        }
    }

    private fun openDonationFragment(requestId: String){
        val bundle = bundleOf(
            RequestDetailsFragment.ARGUMENT_REQUEST_KEY to requestId,
            RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.REQUEST_FULFILLMENT_BINDING
        )
        navController.navigate(R.id.action_notifications_to_requestDetailsFragment, bundle)
    }

    private fun openMyRequestDetailsFragment(requestId: String){
        val bundle = bundleOf(
            RequestDetailsFragment.ARGUMENT_REQUEST_KEY to requestId,
            RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.MY_REQUEST_BINDING
        )
        navController.navigate(R.id.action_notifications_to_requestDetailsFragment, bundle)
    }

    override fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean) {
        try{
            if(!isMyRequest){
                openDonationFragment(donationRequest.id)
            }
            else{
                openMyRequestDetailsFragment(donationRequest.id)
            }
        }
        catch(e: Exception){
            Log.e("NotificationsFragment", "The fragment is already displayed")
        }
    }

    override fun onRequestCardDismissed() {}
}