package com.example.shoryan.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.LoginResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentLoginPasswordBinding
import com.example.shoryan.databinding.LoginBannerBinding
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordLoginFragment : Fragment(), LoadingFragmentHolder {

    private lateinit var navController: NavController
    private lateinit var phoneNumber: String

    val loginViewModel: LoginViewModel by viewModels()

    private lateinit var loginProcess: LiveData<LoginResponse>
    private lateinit var loginObserver: Observer<LoginResponse>

    private var _binding: FragmentLoginPasswordBinding? = null
    private val binding get() = _binding!!
    private var loginBannerBinding: LoginBannerBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginPasswordBinding.inflate(inflater, container, false)
        loginBannerBinding = binding.passwordLoginBanner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loginBannerBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instantiateNavController(view)
        phoneNumber = requireArguments().get("phoneNumber").toString()
        displayPhoneNumber()
        setupPasswordEditText()
        submitPasswordFromKeyboard()
        initializeLoginObserver()
        binding.confirmLoginButton.setOnClickListener { checkLogin() }
        loginBannerBinding!!.loginBack.setOnClickListener { navController.popBackStack() }
    }

    override fun onResume() {
        super.onResume()
        setupPasswordEditText()
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun displayPhoneNumber(){
        binding.enterPasswordSentence.text = resources.getString(R.string.enter_login_password, phoneNumber)
    }

    private fun setupPasswordEditText(){
        binding.passwordEditText.apply {
            requestFocus()
            AndroidUtility.showSoftKeyboard(requireActivity(), this)
            setOnFocusChangeListener { view, _ -> passwordEditTextFocusListener(view) }
        }
    }

    private fun passwordEditTextFocusListener(view: View){
        if(!view.hasFocus()) {
            AndroidUtility.hideSoftKeyboard(requireActivity(), view)
        }
    }

    private fun submitPasswordFromKeyboard(){
        binding.passwordEditText.setOnEditorActionListener{ _, actionId, _ ->
             if(actionId == EditorInfo.IME_ACTION_DONE){
                 binding.confirmLoginButton.performClick()
             }
             false
         }
    }

    private fun initializeLoginObserver(){
        loginObserver = Observer<LoginResponse> {
            toggleLoggingInIndicator()
            it.accessToken?.let { accessToken ->
                handleSuccessfulLogin(accessToken, it.refreshToken!!)
            }
            it.error?.let {
                handleFailedLogin(it.message)
            }
        }
    }

    private fun handleSuccessfulLogin(accessToken: String, refreshToken: String ){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            TokensRefresher.saveTokens(accessToken, refreshToken, requireContext())
            openMainActivity()
        }
    }

    private fun handleFailedLogin(message: ServerError){
        AndroidUtility.displaySnackbarMessage(
            binding.rootLayout,
            resources.getString(message.errorStringResource),
            Snackbar.LENGTH_LONG)
    }

    private fun toggleLoggingInIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    private fun openMainActivity(){
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**
     * This method checks the phone number and password the user has entered and validates
     * the login attempt by checking if this phone number and password exist in the registered users
     * data in the database. If it does exist, the user login is successful and the MainActivity starts.
     * However, if not successful then an error message appears.
     */

    private fun checkLogin() {
        val password = binding.passwordEditText.text.toString().trim()
        if(password.isEmpty()){
            AndroidUtility.displaySnackbarMessage(binding.rootLayout,
                resources.getString(R.string.enter_password),
                Snackbar.LENGTH_LONG)
        }
        else {
            verifyCredentials(phoneNumber, password)
        }
    }

    private fun verifyCredentials(phoneNumber: String, password: String){
        toggleLoggingInIndicator()
        loginProcess = loginViewModel.logUser(phoneNumber, password)
        loginProcess.observe(viewLifecycleOwner, loginObserver)
    }

    override fun onLoadingFragmentDismissed() {
        loginProcess.removeObserver(loginObserver)
    }
}