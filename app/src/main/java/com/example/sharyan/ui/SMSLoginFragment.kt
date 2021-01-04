package com.example.sharyan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sharyan.R
import com.example.sharyan.Utility
import kotlinx.android.synthetic.main.fragment_sms_login.*


class SMSLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPhoneNumber()
        setPinListener()

    }

    override fun onResume() {
        super.onResume()
        setupEditTextFocus()
    }

    private fun setupEditTextFocus(){
        verificationCodeInput.requestFocus()
        Utility.showSoftKeyboard(context, verificationCodeInput)
        verificationCodeInput.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                Utility.hideSoftKeyboard(requireActivity(), v)
            }
        }
    }

    private fun displayPhoneNumber(){
        val phoneNumber = requireArguments().get("phoneNumber")
        enterCodeSentence.text = resources.getString(R.string.enter_login_code, phoneNumber)
    }

    private fun setPinListener(){
        verificationCodeInput.setOnPinEnteredListener {
            Toast.makeText(requireContext(), "Entered full", Toast.LENGTH_SHORT).show()
        }
    }

}