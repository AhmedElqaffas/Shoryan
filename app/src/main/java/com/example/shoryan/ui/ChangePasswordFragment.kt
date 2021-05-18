package com.example.shoryan.ui

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentChangePasswordBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_change_password.*


class ChangePasswordFragment : Fragment() {


    private lateinit var navController: NavController
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

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
        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                turnOnSaveButton()
                turnOffProgressBar()
                AndroidUtility.displaySnackbarMessage(binding.changePasswordLayout,
                    resources.getString(R.string.password_change_success),
                    Snackbar.LENGTH_LONG)
            }
        }
        timer.start()
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
        binding.savePasswordButton.background = resources.getDrawable(R.drawable.button_curved_red)
    }

    private fun turnOffSaveButton() {
        binding.savePasswordButton.isEnabled = false
        binding.savePasswordButton.isClickable = false
        binding.savePasswordButton.background = resources.getDrawable(R.drawable.button_disabled_selector)
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
                binding.currentPasswordLayout.error = "Enter your current password"
                binding.confirmNewPasswordLayout.error = null
                binding.newPasswordLayout.error = null
                return false
            }

            // Checking if the new password has been entered
            newPassword.isEmpty() -> {
                binding.newPasswordLayout.error = "Enter your new password"
                binding.confirmNewPasswordLayout.error = null
                binding.currentPasswordLayout.error = null
                return false
            }

            // Checking if the new password has been confirmed by the user correctly
            newPassword != confirmNewPassword -> {
                binding.confirmNewPasswordLayout.error = "The passwords don't match"
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
}