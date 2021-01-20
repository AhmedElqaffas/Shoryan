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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login_phone.*
import kotlinx.android.synthetic.main.login_banner.*

class LoginPhoneFragment : Fragment(){

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
    }

    override fun onResume() {
        super.onResume()

        phoneEditText.apply{
            requestFocus()
            Utility.showSoftKeyboard(requireActivity(), this)
            this.addTextChangedListener { observePhoneText(it) }
            this.setOnFocusChangeListener { view, _ -> phoneEditTextFocusListener(view) }
        }

        loginBack.setOnClickListener{
            navController.popBackStack()
        }

        loginWithPasswordButton.setOnClickListener {
            goToFragmentIfNumberValid(R.id.action_loginFragment_to_loginPasswordFragment)
        }

        loginWithSMSButton.setOnClickListener {
            goToFragmentIfNumberValid(R.id.action_loginFragment_to_SMSFragment)
        }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    /**
     * Observes changes in the phone number EditText and close keyboard
     * when the user inputs their complete full number.
     */
    private fun observePhoneText(editable: Editable?) {
        if(isValidMobilePhoneEntered(editable.toString())){
            loginScreenLayout.requestFocus()
            phoneTextInputLayout.error = ""
        }
        else{
            phoneTextInputLayout.error = resources.getString(R.string.phone_format_message)
        }
    }

    /**
     * When the phone EditText loses focus, hide keyboard
     */
    private fun phoneEditTextFocusListener(view: View){
        if(!view.hasFocus()) {
            Utility.hideSoftKeyboard(requireActivity(), view)
        }
    }

    private fun goToFragmentIfNumberValid(fragmentId: Int){
        val phoneNumber = getEditTextValue(phoneEditText)
        if(isValidMobilePhoneEntered(phoneNumber)){
            val phoneNumberBundle = bundleOf("phoneNumber" to phoneNumber)
            navController.navigate(fragmentId, phoneNumberBundle)
        }
        else{
            Utility.displaySnackbarMessage(loginScreenLayout,
                resources.getString(R.string.phone_format_message), Snackbar.LENGTH_LONG)
        }
    }

    private fun isValidMobilePhoneEntered(phoneNumber: String): Boolean =
        phoneNumber.length == resources.getInteger(R.integer.phone_number_length)
            && phoneNumber.matches(Regex("01[0-9]+"))

    private fun getEditTextValue(editText: EditText):String =  editText.text.toString().trim()
}