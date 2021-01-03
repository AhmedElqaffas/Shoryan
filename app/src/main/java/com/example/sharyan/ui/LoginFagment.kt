package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.sharyan.R
import com.example.sharyan.Utility
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_new_request.*
import kotlinx.coroutines.*


class LoginFragment : Fragment(), LoadingFragmentHolder {

    private lateinit var navController: NavController
    private val usersLoginViewModel: UsersLoginViewModel by navGraphViewModels(R.id.landing_nav_graph)
    private var loginVerificationJob: Job? = null

    override fun onDestroy() {
        super.onDestroy()
        loginVerificationJob?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
    }

    override fun onResume() {
        super.onResume()

        phoneEditText.apply{
            this.addTextChangedListener { observePhoneText(it) }
            this.setOnFocusChangeListener { view, _ -> focusListener(view) }
        }

        passwordEditText.setOnFocusChangeListener { view, _ ->   focusListener(view) }

        submitInfoFromPasswordEditText()

        confirmLoginButton.setOnClickListener {
            checkLogin()
        }

        loginBack.setOnClickListener{
            navController.popBackStack()
        }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    /**
     * Observes changes in the phone number EditText and shifts focus to the password EditText
     * when the user inputs the complete full number, i.e. when the EditText length reaches the
     * max length specified in the XML
    */
    private fun observePhoneText(editable: Editable?) {
        if(editable?.length == resources.getInteger(R.integer.phone_number_length)){
            passwordEditText.requestFocus()
        }
    }

    /**
     * When the user clicks "Done" while in the password EditText, a click is automatically performed
     * on the login button instead of letting the user manually click it
     */
    private fun submitInfoFromPasswordEditText(){
        passwordEditText.setOnEditorActionListener{ _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                confirmLoginButton.performClick()
            }
            false
        }
    }

    /**
     * Hides the keyboard whenever the EditTexts lose focus
     */
    private fun focusListener(view: View){
        if(!phoneEditText.hasFocus() && !passwordEditText.hasFocus())
            Utility.hideSoftKeyboard(requireActivity(), view)
    }

    /**
     * This method checks the phone number and password the user has entered and validates
     * the login attempt by checking if this phone number and password exist in the registered users
     * data in the database. If it does exist, the user login is successful and the MainActivity starts.
     * However, if not successful then an error message appears.
     */

    private fun checkLogin() {

        val phoneNumber = getEditTextValue(phoneEditText)
        val password = getEditTextValue(passwordEditText)

        if(!areAllInputsProvided(phoneNumber, password)){
            displayError("من فضلك تأكّد من ادخال رقم الهاتف و كلمة السر")
        }

        else {
            verifyCredentials(phoneNumber, password)
        }
    }

    private fun getEditTextValue(editText: EditText):String{
        return editText.text.toString().trim()
    }

    private fun areAllInputsProvided(phoneNumber: String, password: String): Boolean{
        return phoneNumber.isNotEmpty() and password.isNotEmpty()
    }

    private fun displayError(error: String){
        Snackbar.make(loginScreenLayout, error, Snackbar.LENGTH_LONG)
            .setAction("حسناً") {
                // By default, the snackbar will be dismissed
            }
            .show()
    }

    private fun verifyCredentials(phoneNumber: String, password: String){
        toggleLoggingInIndicator()
        loginVerificationJob = CoroutineScope(Dispatchers.Main).launch {
            usersLoginViewModel.verifyCredentials(phoneNumber, password)
                .observe(this@LoginFragment, {
                    toggleLoggingInIndicator()
                    it.user?.let { openMainActivity() }
                    it.error?.let { message ->
                        it.error
                        displayError(message)
                    }
                })
        }
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

    override fun onLoadingFragmentDismissed() {
        activity?.finish()
    }
}