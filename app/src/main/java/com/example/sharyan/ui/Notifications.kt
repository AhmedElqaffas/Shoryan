package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.example.sharyan.R
import com.example.sharyan.recyclersAdapters.NotificationsRecyclerAdapter
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_notifications.*

class Notifications : Fragment() {

    private lateinit var notificationsRecyclerAdapter: NotificationsRecyclerAdapter
    private val notificationsViewModel: NotificationsViewModel by navGraphViewModels(R.id.main_nav_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onResume() {
        super.onResume()
        setToolbarText(resources.getString(R.string.notifications))
        initializeRecyclerViewAdapter()
        showNotifications()
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun initializeRecyclerViewAdapter(){
        notificationsRecyclerAdapter = NotificationsRecyclerAdapter()
        notificationsRecycler.adapter = notificationsRecyclerAdapter
    }

    private fun showNotifications(){
        notificationsViewModel.getNotifications().observe(viewLifecycleOwner){
            notificationsRecyclerAdapter.setNotificationsList(it)
        }
    }
}