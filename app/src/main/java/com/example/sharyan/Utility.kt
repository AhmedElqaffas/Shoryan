package com.example.sharyan

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_request_fulfillment.*
import kotlin.reflect.KFunction0


class Utility {
    companion object{
        fun showSoftKeyboard(context: Context?, view: View){
            val imm: InputMethodManager? =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun hideSoftKeyboard(activity: Activity, view: View){
            if(activity.currentFocus !is EditText) {
                val inputMethodManager = getSystemService(activity, InputMethodManager::class.java)!!
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun displaySnackbarMessage(layout: View,  message: String, duration : Int){
            val snackbar =  Snackbar.make(layout, message, duration)
                .setAction(R.string.ok) {
                    // By default, the snackbar will be dismissed
                }
                .setActionTextColor(layout.context.resources.getColor(R.color.colorAccent))
            ViewCompat.setLayoutDirection(snackbar.view, ViewCompat.LAYOUT_DIRECTION_RTL)
            snackbar.show()
        }

        fun showTryAgainSnackbar(view: View, message: String, whatToTry: () -> Unit){
            val snackbar =  Snackbar.make(view,
                message,
                Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(R.string.try_again) {
                whatToTry()
            }
            ViewCompat.setLayoutDirection(snackbar.view, ViewCompat.LAYOUT_DIRECTION_RTL)
            snackbar.show()
        }
    }
}