package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.databinding.FragmentSmsBinding
import com.example.shoryan.databinding.LoginBannerBinding

class SMSFragment : Fragment() {

    private lateinit var navController: NavController
    private var phoneNumber = ""

    private var _binding: FragmentSmsBinding? = null
    private val binding get() = _binding!!
    private var loginBannerBinding: LoginBannerBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSmsBinding.inflate(inflater, container, false)
        loginBannerBinding = binding.smsLoginBanner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loginBannerBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeNavController(view)
        phoneNumber = requireArguments().get("phoneNumber") as String
        displayPhoneNumber()
        setPinListener()
        loginBannerBinding!!.loginBack.setOnClickListener { navController.popBackStack() }
    }

    private fun initializeNavController(view: View) {
        navController = Navigation.findNavController(view)
    }

    private fun displayPhoneNumber(){
        binding.enterCodeSentence.text = resources.getString(R.string.enter_code, phoneNumber)
    }

    private fun setPinListener(){
        binding.verificationCodeInput.setOnPinEnteredListener {
            binding.verificationCodeInput.clearFocus()
            Toast.makeText(requireContext(), "Entered full", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupEditTextFocus()
    }

    private fun setupEditTextFocus(){
        binding.verificationCodeInput.requestFocus()
        AndroidUtility.showSoftKeyboard(context, binding.verificationCodeInput)
        binding.verificationCodeInput.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                AndroidUtility.hideSoftKeyboard(requireActivity(), v)
            }
        }
    }
}