package com.example.shoryan.ui

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.AndroidUtility
import com.example.shoryan.InputValidator
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentLoginPhoneBinding
import com.example.shoryan.databinding.LoginBannerBinding
import com.example.shoryan.getStringWithoutAdditionalSpaces

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

    override fun onResume() {
        super.onResume()
        setupPhoneEditText()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)

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
            AndroidUtility.showSoftKeyboard(context, this)
            this.addTextChangedListener { observePhoneText(it) }
            this.setOnFocusChangeListener { view, _ -> phoneEditTextFocusListener(view) }
        }
    }

    /**
     * Observes changes in the phone number EditText and closes keyboard
     * when the user inputs their full number.
     */
    private fun observePhoneText(editable: Editable?) {
        if(InputValidator.isValidMobilePhoneEntered(editable.toString())){
            binding.loginScreenLayout.requestFocus()
            binding.phoneTextInputLayout.error = ""
        }
        else{
            binding.phoneTextInputLayout.error = resources.getString(R.string.phone_format_message)
        }
    }

    private fun phoneEditTextFocusListener(view: View){
        if(!view.hasFocus()) {
            AndroidUtility.hideSoftKeyboard(requireActivity(), view)
        }
    }

    private fun goToFragmentIfNumberValid(fragmentId: Int){
        val phoneNumber = binding.phoneEditText.getStringWithoutAdditionalSpaces()
        if(InputValidator.isValidMobilePhoneEntered(phoneNumber)){
            val phoneNumberBundle = bundleOf("phoneNumber" to phoneNumber)
            navController.navigate(fragmentId, phoneNumberBundle)
        }
        else{
            AndroidUtility.displayAlertDialog(requireContext(), resources.getString(R.string.phone_format_message))
        }
    }
}