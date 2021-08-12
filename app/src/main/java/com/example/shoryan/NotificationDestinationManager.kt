package com.example.shoryan

import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.NotificationDestinationCode
import com.example.shoryan.ui.MainActivity
import com.example.shoryan.ui.RequestDetailsFragment
import com.google.gson.Gson

object NotificationDestinationManager {

    fun createNavDeepLink(context: Context, notificationData: Map<String, String>): NavDeepLinkBuilder{
        val navLink = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.main_nav_graph)
            .setComponentName(MainActivity::class.java)
        setDestination(navLink, notificationData)
        return navLink
    }

    private fun setDestination(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>){
        notificationData["code"]?.let{ code ->
            when(NotificationDestinationCode.fromString(code)){
                NotificationDestinationCode.MY_REQUEST -> goToMyRequestFragment(navLink, notificationData)
                NotificationDestinationCode.REQUEST_FULFILLMENT -> goToRequestFulfillmentFragment(navLink, notificationData)
                NotificationDestinationCode.PROFILE -> goToProfileFragment(navLink)
                else -> goToHomeFragment(navLink)
            }
        }
    }

    private fun goToMyRequestFragment(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>) {
        val gson = Gson()
        val request = gson.fromJson(notificationData["request"], DonationRequest::class.java)
        navLink.setDestination(R.id.requestDetailsFragment)
            .setArguments(bundleOf(
                RequestDetailsFragment.ARGUMENT_REQUEST_KEY to request.id,
                RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.MY_REQUEST_BINDING
            ))
    }

    private fun goToRequestFulfillmentFragment(navLink: NavDeepLinkBuilder, notificationData: Map<String, String>) {
        val gson = Gson()
        val request = gson.fromJson(notificationData["request"], DonationRequest::class.java)
        navLink.setDestination(R.id.requestDetailsFragment)
            .setArguments(bundleOf(
                RequestDetailsFragment.ARGUMENT_REQUEST_KEY to request.id,
                RequestDetailsFragment.ARGUMENT_BINDING_KEY to RequestDetailsFragment.REQUEST_FULFILLMENT_BINDING
            ))
    }

    private fun goToProfileFragment(navLink: NavDeepLinkBuilder) {
        navLink.setDestination(R.id.profile)
    }

    private fun goToHomeFragment(navLink: NavDeepLinkBuilder){
        navLink.setDestination(R.id.home)
    }
}