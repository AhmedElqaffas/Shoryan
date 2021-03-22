package com.example.shoryan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.example.shoryan.R
import com.example.shoryan.databinding.AppbarBinding
import com.example.shoryan.databinding.FragmentNotificationsBinding
import com.example.shoryan.ui.recyclersAdapters.NotificationsRecyclerAdapter
import com.example.shoryan.viewmodels.NotificationsViewModel

class NotificationsFragment : Fragment() {

    private lateinit var notificationsRecyclerAdapter: NotificationsRecyclerAdapter
    private val notificationsViewModel: NotificationsViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        toolbarBinding = binding.notificationsAppbar
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        toolbarBinding = null
    }

    override fun onResume() {
        super.onResume()
        setToolbarText(resources.getString(R.string.notifications))
        initializeRecyclerViewAdapter()
        showNotifications()
    }

    private fun setToolbarText(text: String){
        toolbarBinding!!.toolbarText.text = text
    }

    private fun initializeRecyclerViewAdapter(){
        notificationsRecyclerAdapter = NotificationsRecyclerAdapter()
        binding.notificationsRecycler.adapter = notificationsRecyclerAdapter
    }

    private fun showNotifications(){
        notificationsViewModel.getNotifications().observe(viewLifecycleOwner){
            notificationsRecyclerAdapter.setNotificationsList(it)
        }
    }
}