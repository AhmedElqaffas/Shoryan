package com.example.shoryan.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.LocaleHelper
import com.example.shoryan.R
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentProfileBinding
import com.example.shoryan.interfaces.LocaleChangerHolder
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.ProfileViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(), LocaleChangerHolder {

    private lateinit var navController: NavController
    val tokensViewModel: TokensViewModel by viewModels()
    val profileViewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Getting the navigation controller object
        navController = Navigation.findNavController(view)

        // Go to Account Info screen when the account info button is clicked
        binding.accountInfoButton.setOnClickListener {
            navController.navigate(R.id.action_profile_to_accountInfoFragment)
        }

        // Go to Change Password screen when the change password button is clicked
        binding.changePasswordButton.setOnClickListener {
            navController.navigate(R.id.action_profile_to_changePasswordFragment)
        }

        // Open LanguageFragment dialog box when the change language button is clicked
        binding.changeLanguageButton.setOnClickListener {
            openLanguageFragment()
        }

        // Set language text with the current language chosen
        binding.languageTextView.text = when (LocaleHelper.getLanguage(requireContext())) {
            "ar" -> "العربية"
            "en" -> "English"
            else -> ""
        }

        // Log out of the account when the Log out button is clicked
        binding.logoutButton.setOnClickListener {
            logoutFromAccount()
        }
    }

    private fun fetchUserData(isRefreshing: Boolean) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val response = profileViewModel.getProfileData(isRefreshing)
            // Handle the error if exists
            response.error?.let {
                handleError(it.message)
            }
        }
    }

    private fun handleError(error: ServerError) {
        if (error == ServerError.JWT_EXPIRED) {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let {
                    forceLogOut()
                }
            }
        } else {
            error.doErrorAction(binding.root)
        }
    }

    private fun forceLogOut() {
        Toast.makeText(requireContext(), resources.getString(R.string.re_login), Toast.LENGTH_LONG)
            .show()
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun refresh() {
        fetchUserData(true)
    }

    private fun logoutFromAccount() {
        /**
         *  This function logs the user out of his/her account.
         *  In order to do this, the tokens must be cleared and the LandingActivity must be restarted
         */

        GlobalScope.launch {
            //Clearing stored access and refresh tokens
            TokensRefresher.clearCachedTokens(requireContext())
        }

        // Show logout message to user
        Toast.makeText(
            requireContext(),
            resources.getString(R.string.logged_out_message),
            Toast.LENGTH_LONG
        ).show()

        // Restarting LandingActivity
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    private fun openLanguageFragment() {
        LanguageFragment().show(childFragmentManager, "language")
    }

    /**
     * This callback method is called when the app's language has been changed by the user.
     * The activity has to be restarted after the language change takes place.
     */
    override fun onLocaleChanged(newLanguageTag: String) {
        // Changing the app's language
        updateLocale(newLanguageTag)

        // Restarting activity
        requireActivity().recreate()
    }

    private fun updateLocale(newLanguageTag: String) {
        LocaleHelper.persist(requireContext(), newLanguageTag)
    }

}