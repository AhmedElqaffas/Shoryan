package com.example.shoryan.ui

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility
import com.example.shoryan.InputValidator
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentChangePasswordBinding
import com.example.shoryan.viewmodels.AccountInfoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChangePasswordFragment : Fragment() {


    private lateinit var navController: NavController
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val accountInfoViewModel: AccountInfoViewModel by navGraphViewModels(R.id.main_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
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

        // Go to profile settings screen when the back button is clicked
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        // Observing the viewmodel events
        observeViewModelEvents()

        // Setting up the save new password button
        binding.savePasswordButton.setOnClickListener {
            if (checkIfPasswordValid()) {
                turnOnProgressBar()
                turnOffSaveButton()
                saveNewPassword()
            }

        }
    }

    private fun saveNewPassword() {
        /**
         * This method saves the changes made to the user's password through an API call to the backend.
         */

        // Getting the values the user has entered
        val oldPassword = binding.currentPasswordEditText.text.toString().trim()
        val newPassword = binding.newPasswordEditText.text.toString().trim()

        // Making the API call
        accountInfoViewModel.changeUserPassword(accountInfoViewModel.createChangePasswordQuery(oldPassword, newPassword))
    }

    private fun turnOnProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun turnOffProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun turnOnSaveButton() {
        binding.savePasswordButton.isEnabled = true
        binding.savePasswordButton.isClickable = true
        binding.savePasswordButton.setBackgroundResource(R.drawable.button_curved_red)
    }

    private fun turnOffSaveButton() {
        binding.savePasswordButton.isEnabled = false
        binding.savePasswordButton.isClickable = false
        binding.savePasswordButton.setBackgroundResource(R.drawable.button_disabled_selector)
    }


    private fun checkIfPasswordValid() : Boolean {
        /**
         *  This method checks if the user has entered valid values for the change password operation.
         */

        // Collecting the input data from the user into variables
        val oldPassword = binding.currentPasswordEditText.text.toString().trim()
        val newPassword = binding.newPasswordEditText.text.toString().trim()
        val confirmNewPassword = binding.confirmNewPasswordEditText.text.toString().trim()


        when {
            // Checking if the old password has been entered
            oldPassword.isEmpty() -> {
                binding.currentPasswordLayout.error = resources.getString(R.string.enter_old_password)
                binding.confirmNewPasswordLayout.error = null
                binding.newPasswordLayout.error = null
                return false
            }

            // Checking if the new password has been entered
            newPassword.isEmpty() -> {
                binding.newPasswordLayout.error = resources.getString(R.string.enter_new_password)
                binding.confirmNewPasswordLayout.error = null
                binding.currentPasswordLayout.error = null
                return false
            }

            // Checking if the new password is valid
            !(InputValidator.isValidPasswordEntered(newPassword)) ->{
                binding.newPasswordLayout.error = resources.getString(R.string.password_format_message)
                binding.confirmNewPasswordLayout.error = null
                binding.currentPasswordLayout.error = null
                return false
            }

            // Checking if the new password has been confirmed by the user correctly
            newPassword != confirmNewPassword -> {
                binding.confirmNewPasswordLayout.error = resources.getString(R.string.password_mismatch)
                binding.newPasswordLayout.error =  null
                binding.currentPasswordLayout.error = null
                return false
            }

            else -> { // No errors occurred
                binding.confirmNewPasswordLayout.error = null
                binding.newPasswordLayout.error = null
                binding.currentPasswordLayout.error = null
                return true
            }
        }

    }

    private fun observeViewModelEvents(){
        accountInfoViewModel.passwordEventsFlow.onEach {
            when(it){
                is AccountInfoViewModel.ChangePasswordViewEvent.ShowSnackBarFromResource -> onFailureResponse(it.textResourceId)
                AccountInfoViewModel.ChangePasswordViewEvent.ChangedPasswordsSuccessfully -> onSuccessfulResponse()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showSnackbar(message: String){
        AndroidUtility.displaySnackbarMessage(binding.rootLayout, message, Snackbar.LENGTH_LONG)
    }

    /**
     * This callback method is called when an error occurs while changing the password.
     * It displays an error message to the user and toggles the loading indicators.
     */
    private fun onFailureResponse(resourceID: Int){
        showSnackbar(resources.getString(resourceID))
        turnOffProgressBar()
        turnOnSaveButton()
    }

    /**
     * This callback method is called when the password has been changed successfully.
     * It displays a success message to the user and toggles the loading indicators.
     */
    private fun onSuccessfulResponse(){
        showSnackbar(resources.getString(R.string.password_change_success))
        turnOffProgressBar()
        turnOnSaveButton()
        navController.navigateUp()
    }
}