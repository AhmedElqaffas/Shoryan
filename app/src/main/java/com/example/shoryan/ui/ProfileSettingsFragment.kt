package com.example.shoryan.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.DataStoreUtil
import com.example.shoryan.LocaleHelper
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentProfileBinding
import com.example.shoryan.databinding.FragmentProfileSettingsBinding
import com.example.shoryan.interfaces.LocaleChangerHolder
import com.example.shoryan.repos.TokensRefresher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfileSettingsFragment : Fragment(), LocaleChangerHolder {
    private lateinit var navController: NavController
    private var _binding: FragmentProfileSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Getting the navigation controller object
        navController = Navigation.findNavController(view)

        // Go to profile screen when the back button is clicked
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        // Log out of the account when the Log out button is clicked
        binding.logoutButton.setOnClickListener{
            logoutFromAccount()
        }

        // Go to ChangePasswordFragment when the change password button is clicked
        binding.changePasswordButton.setOnClickListener {
            navController.navigate(R.id.action_profileSettingsFragment_to_changePasswordFragment)
        }

        // Open LanguageFragment dialog box when the change language button is clicked
        binding.changeLanguageButton.setOnClickListener {
            openLanguageFragment()
        }
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

    private fun openLanguageFragment(){
        LanguageFragment().show(childFragmentManager, "language")
    }

    override fun onLocaleChanged(newLanguageTag: String){
        /**
         * This callback method is called when the app's language has been changed by the user.
         * The application has to be restarted after the language change takes place.
         */

        // Changing the app's language
        updateLocale(newLanguageTag)

        // Restarting LandingActivity
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun updateLocale(newLanguageTag: String) {
        LocaleHelper.persist(requireContext(), newLanguageTag)
    }

}