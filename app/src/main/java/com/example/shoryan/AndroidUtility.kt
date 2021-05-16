package com.example.shoryan

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.example.shoryan.ui.LandingActivity
import com.google.android.material.snackbar.Snackbar


class AndroidUtility {
    companion object{

        const val SHARED_PREFERENCES = "preferences"

        fun showSoftKeyboard(context: Context?, view: View){
            val imm: InputMethodManager? =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun hideSoftKeyboard(context: Context, view: View){
            val inputMethodManager = getSystemService(context, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun displaySnackbarMessage(layout: View,  message: String, duration : Int){
            val snackbar =  Snackbar.make(layout, message, duration)
                .setAction(R.string.ok) {
                    // By default, the snackbar will be dismissed
                }
                .setActionTextColor(layout.context.resources.getColor(R.color.colorAccent))
            snackbar.show()
        }

        fun makeTryAgainSnackbar(view: View, message: String, whatToTry: () -> Unit): Snackbar{
            val snackbar =  Snackbar.make(view,
                message,
                Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(R.string.try_again) {
                whatToTry()
            }
            return snackbar
        }

        fun forceLogOut(context: Context){
            Toast.makeText(context, context.resources.getString(R.string.re_login), Toast.LENGTH_LONG).show()
            val intent = Intent(context, LandingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun getScreenWidth(context: Context): Dp =
            context.resources.displayMetrics.run { return@run (1.dp *(widthPixels / density) ) }
        fun getScreenHeight(context: Context): Dp =
            context.resources.displayMetrics.run { return@run (1.dp *(heightPixels / density) ) }

    }
}