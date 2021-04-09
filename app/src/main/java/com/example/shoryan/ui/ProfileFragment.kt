package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shoryan.AndroidUtility
import com.example.shoryan.data.ServerError
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.databinding.FragmentProfileBinding
import com.example.shoryan.di.MyApplication
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.ProfileViewModel
import com.example.shoryan.viewmodels.ProfileViewModelFactory
import com.example.shoryan.viewmodels.TokensViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private val bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
            .getRetrofitClient()
            .create(RetrofitBloodDonationInterface::class.java)

    @Inject
    lateinit var tokensViewModel: TokensViewModel
    private val profileViewModel: ProfileViewModel by viewModels{ProfileViewModelFactory(bloodDonationAPI)}

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.profileComponent().create().inject(this)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewmodel = profileViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserData(false)
        (binding.refreshLayout).setOnRefreshListener { refresh() }
    }

    private fun fetchUserData(isRefreshing: Boolean){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed{
            val response = profileViewModel.getProfileData(isRefreshing)
            // Handle the error if exists
            response.error?.let{
                handleError(it.message)
            }
        }
    }

    private suspend fun handleError(error: ServerError) {
        if(error == ServerError.JWT_EXPIRED){
            tokensViewModel.getNewAccessToken(requireContext())
        }
        else{
            error.doErrorAction(binding.rootLayout)
        }
    }

    private fun refresh(){
        fetchUserData(true)
    }
}