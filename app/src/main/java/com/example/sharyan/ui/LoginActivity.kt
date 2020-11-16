package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.sharyan.R
import com.example.sharyan.Utility
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
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

    private fun checkLogin(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}