package com.example.shoryan

import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.shoryan.ui.MainActivity
import com.example.shoryan.ui.RequestDetailsFragment

object NotificationDestinationManager {

    val MY_REQUEST = 0
    val REQUEST_FULFILLMENT = 1

    fun createNavDeepLink(context: Context, notificationData: Map<String, String>): NavDeepLinkBuilder{
        val navLink = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.main_nav_graph)
            .setComponentName(MainActivity::class.java)
        setDestination(navLink, notificationData)
        return navLink
    }

    private fun setDestination(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>){
        when(notificationData["code"]?.toInt()){
            MY_REQUEST -> goToMyRequestFragment(navLink, notificationData)
            REQUEST_FULFILLMENT -> goToRequestFulfillmentFragment(navLink, notificationData)
            else -> goToHomeFragment(navLink)
        }
    }

    private fun goToMyRequestFragment(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>) {
        navLink.setDestination(R.id.requestDetailsFragment)
            .setArguments(bundleOf(
                RequestDetailsFragment.ARGUMENT_REQUEST_KEY to notificationData["requestId"],
                RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.MY_REQUEST_BINDING
            ))
    }

    private fun goToRequestFulfillmentFragment(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>) {
        navLink.setDestination(R.id.requestDetailsFragment)
            .setArguments(bundleOf(
                RequestDetailsFragment.ARGUMENT_REQUEST_KEY to notificationData["requestId"],
                RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.REQUEST_FULFILLMENT_BINDING
            ))
    }

    private fun goToHomeFragment(navLink: NavDeepLinkBuilder){
        navLink.setDestination(R.id.home)
    }
}