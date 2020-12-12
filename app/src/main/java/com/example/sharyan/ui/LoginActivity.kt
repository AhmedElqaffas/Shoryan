package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.sharyan.R
import com.example.sharyan.Utility
import com.example.sharyan.data.User
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.UsersRetriever
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
   lateinit var usersLoginViewModel : UsersLoginViewModel
   var usersList : List<User> = listOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usersLoginViewModel = ViewModelProvider(this).get(UsersLoginViewModel::class.java)

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
        val phoneNumber = phoneEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (phoneNumber.isEmpty() or password.isEmpty()) {
            Toast.makeText(
                this, "Please make sure to fill missing values!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        else { // Get users list from database or cache
            CoroutineScope(Dispatchers.Main).launch {
                usersLoginViewModel.getAllUsers().observe(this@LoginActivity,  {
                    var count = 0
                    for(user in it){
                        // phoneNumber is 1097049699  password is pass1
                        if((user.phoneNumber == phoneNumber.toInt()) and (user.password == password)){
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        else{
                            count += 1
                        }
                        if(count == it.size) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Error! couldn't login. Please make sure you have entered the correct phone number and password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } })
            }



             }


            }

}