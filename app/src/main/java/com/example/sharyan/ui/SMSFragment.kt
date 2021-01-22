package com.example.sharyan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import com.example.sharyan.Utility
import kotlinx.android.synthetic.main.fragment_sms.*
import kotlinx.android.synthetic.main.login_banner.*


class SMSFragment : Fragment() {

    private lateinit var navController: NavController
    private var phoneNumber = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeNavController(view)
        phoneNumber = requireArguments().get("phoneNumber") as String
        displayPhoneNumber()
        setPinListener()
        loginBack.setOnClickListener { navController.popBackStack() }
    }

    private fun initializeNavController(view: View) {
        navController = Navigation.findNavController(view)
    }

    private fun displayPhoneNumber(){
        enterCodeSentence.text = resources.getString(R.string.enter_code, phoneNumber)
    }

    private fun setPinListener(){
        verificationCodeInput.setOnPinEnteredListener {
            verificationCodeInput.clearFocus()
            Toast.makeText(requireContext(), "Entered full", Toast.LENGTH_SHORT).show()
        }
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
}