package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharyan.R
import kotlinx.android.synthetic.main.appbar.*

class Home : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        setToolbarText(resources.getString(R.string.home))
        //setRequestsFragment()
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun setRequestsFragment(){
        if(childFragmentManager.findFragmentByTag("requests") == null){
            childFragmentManager.beginTransaction().replace(
                R.id.requestsContainer, RequestsRecyclerFragment(),
                "requests").commit()
        }

    }

}