package com.example.shoryan.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.repos.NewRequestRepo
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.ui.LandingActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class ServerError(val errorStringResource: Int) {
    INVALID_CREDENTIALS(R.string.invalid_credentials),
    ACCOUNT_NOT_VERIFIED(R.string.account_unverified),
    INVALID_CODE(R.string.invalid_code),
    MISSING_PARAMETER(R.string.fill_all_data),
    USER_NOT_FOUND(R.string.user_not_found),
    INVALID_FORMAT(R.string.invalid_input),
    PHONE_NUMBER_REQUIRED(R.string.connection_error),
    JWT_EXPIRED(R.string.connection_error){
        override fun doErrorAction(context: Context) {
            AndroidUtility.forceLogOut(context)
        }
      },

    // UnAuthorized error -> The user should be forcefully logged out
    UNAUTHORIZED(R.string.re_login) {
        override fun doErrorAction(rootView: View) {
            val context = rootView.context
            clearTokensAndLogUserOut(context)
        }

        override fun doErrorAction(context: Context) {
            clearTokensAndLogUserOut(context)
        }

        private fun clearTokensAndLogUserOut(context: Context){
            GlobalScope.launch {
                TokensRefresher.clearCachedTokens(context)
                Toast.makeText(
                    context,
                    context.resources.getString(errorStringResource),
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(context, LandingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    },
    USER_ALREADY_EXISTS(R.string.user_already_exists),
    AGE_CHECK_FAILS(R.string.age_check_failed),
    BREAK_TIME_CHECK_FAILS(R.string.frequent_donation),
    INCOMPATIBLE_BLOOD_TYPE(R.string.incompatible_blood_type),
    USER_GOING_TO_ANOTHER_REQUEST(R.string.request_already_pending),
    JWT_NOT_ACTIVE(R.string.connection_error),
    INTERNAL_SERVER_ERROR(R.string.connection_error),
    CONNECTION_ERROR(R.string.connection_error),

    // Create New Request errors
    REQUESTS_DAILY_LIMIT(R.string.cant_request_today);

    open fun doErrorAction(rootView: View){
        AndroidUtility.displaySnackbarMessage(rootView,
            rootView.context.resources.getString(this.errorStringResource), Snackbar.LENGTH_LONG)
    }

    open fun doErrorAction(context: Context){}
}