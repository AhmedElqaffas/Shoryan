package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharyan.R
import kotlinx.android.synthetic.main.appbar.*

class Notifications : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onResume() {
        super.onResume()
        setToolbarText(resources.getString(R.string.notifications))
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }
}