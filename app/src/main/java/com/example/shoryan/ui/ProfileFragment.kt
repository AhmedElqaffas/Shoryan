package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.R
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentProfileBinding
import com.example.shoryan.di.MyApplication
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.viewmodels.ProfileViewModel
import com.example.shoryan.viewmodels.ProfileViewModelFactory
import com.example.shoryan.viewmodels.TokensViewModel
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

    private fun refresh(){
        fetchUserData(true)
    }
}