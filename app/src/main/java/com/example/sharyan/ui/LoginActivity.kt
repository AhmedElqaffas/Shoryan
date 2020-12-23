package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.example.sharyan.R
import com.example.sharyan.Utility
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*


class LoginActivity : AppCompatActivity(), LoadingFragmentHolder {

   private val usersLoginViewModel: UsersLoginViewModel by viewModels()
    private var loginVerificationJob: Job? = null

    override fun onDestroy() {
        super.onDestroy()
        loginVerificationJob?.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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
            finish()
        }
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
            Utility.hideSoftKeyboard(this@LoginActivity, view)
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
        Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
    }

    private fun verifyCredentials(phoneNumber: String, password: String){
        toggleLoggingInIndicator()
        loginVerificationJob = CoroutineScope(Dispatchers.Main).launch {
            usersLoginViewModel.verifyCredentials(phoneNumber, password)
                .observe(this@LoginActivity, {
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
        val loadingFragment: DialogFragment? = supportFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(supportFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    private fun openMainActivity(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onLoadingFragmentDismissed() {
        this.finish()
    }
}