package com.example.sharyan.ui

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import com.example.sharyan.Utility
import com.example.sharyan.databinding.FragmentLoginPhoneBinding
import com.example.sharyan.databinding.LoginBannerBinding
import com.google.android.material.snackbar.Snackbar

class LoginPhoneFragment : Fragment(){

    private lateinit var navController: NavController

    private var _binding: FragmentLoginPhoneBinding? = null
    private val binding get() = _binding!!
    private var bannerBinding: LoginBannerBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginPhoneBinding.inflate(inflater, container, false)
        bannerBinding = binding.includes
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerBinding = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        setupPhoneEditText()

        bannerBinding!!.loginBack.setOnClickListener{
            navController.popBackStack()
        }

        binding.loginWithPasswordButton.setOnClickListener {
            goToFragmentIfNumberValid(R.id.action_loginFragment_to_loginPasswordFragment)
        }

        binding.loginWithSMSButton.setOnClickListener {
            goToFragmentIfNumberValid(R.id.action_loginFragment_to_SMSFragment)
        }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setupPhoneEditText(){
        binding.phoneEditText.apply{
            requestFocus()
            Utility.showSoftKeyboard(requireActivity(), this)
            this.addTextChangedListener { observePhoneText(it) }
            this.setOnFocusChangeListener { view, _ -> phoneEditTextFocusListener(view) }
        }
    }

    /**
     * Observes changes in the phone number EditText and closes keyboard
     * when the user inputs their full number.
     */
    private fun observePhoneText(editable: Editable?) {
        if(isValidMobilePhoneEntered(editable.toString())){
            binding.loginScreenLayout.requestFocus()
            binding.phoneTextInputLayout.error = ""
        }
        else{
            binding.phoneTextInputLayout.error = resources.getString(R.string.phone_format_message)
        }
    }

    private fun isValidMobilePhoneEntered(phoneNumber: String): Boolean =
        phoneNumber.length == resources.getInteger(R.integer.phone_number_length)
                && phoneNumber.matches(Regex("01[0-9]+"))


    private fun phoneEditTextFocusListener(view: View){
        if(!view.hasFocus()) {
            Utility.hideSoftKeyboard(requireActivity(), view)
        }
    }

    private fun goToFragmentIfNumberValid(fragmentId: Int){
        val phoneNumber = getEditTextValue(binding.phoneEditText)
        if(isValidMobilePhoneEntered(phoneNumber)){
            val phoneNumberBundle = bundleOf("phoneNumber" to phoneNumber)
            navController.navigate(fragmentId, phoneNumberBundle)
        }
        else{
            Utility.displaySnackbarMessage(binding.loginScreenLayout,
                resources.getString(R.string.phone_format_message), Snackbar.LENGTH_LONG)
        }
    }

    private fun getEditTextValue(editText: EditText):String =  editText.text.toString().trim()
}