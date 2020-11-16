package com.example.sharyan

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService


class Utility {
    companion object{
        fun hideSoftKeyboard(activity: Activity, view: View){
            if(activity.currentFocus !is EditText) {
                val inputMethodManager = getSystemService(activity, InputMethodManager::class.java)!!
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}