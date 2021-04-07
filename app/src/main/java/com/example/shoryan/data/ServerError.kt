package com.example.shoryan.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.ui.LandingActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class ServerError(val errorStringResource: Int) {
    INVALID_CREDENTIALS(R.string.invalid_credentials),
    MISSING_PARAMETER(R.string.fill_all_data),
    USER_NOT_FOUND(R.string.user_not_found),
    INVALID_FORMAT(R.string.connection_error),
    JWT_EXPIRED(R.string.connection_error)
    {
        override fun doErrorAction(activity: Activity){
        }
    },
    // UnAuthorized error -> The user should log in again
    UNAUTHORIZED(R.string.re_login)
    {
        override fun doErrorAction(activity: Activity){
            GlobalScope.launch{
                TokensRefresher.clearCachedTokens(activity)
                Toast.makeText(activity, activity.resources.getString(errorStringResource), Toast.LENGTH_LONG).show()
                val intent = Intent(activity, LandingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
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
    CONNECTION_ERROR(R.string.connection_error);

    open fun doErrorAction(activity: Activity){
        AndroidUtility.displaySnackbarMessage(activity.findViewById(R.id.rootLayout),
            activity.resources.getString(this.errorStringResource), Snackbar.LENGTH_LONG)
    }

    companion object {
        fun fromResource(errorStringResource: Int) =  try{
            values().first { it.errorStringResource == errorStringResource }
        }catch (e: NoSuchElementException){
            null
        }
    }
}