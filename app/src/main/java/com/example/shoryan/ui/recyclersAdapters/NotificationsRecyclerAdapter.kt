package com.example.shoryan.ui.recyclersAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.R
import com.example.shoryan.data.DonationNotification
import kotlinx.android.synthetic.main.item_notification.view.*

class NotificationsRecyclerAdapter:
    RecyclerView.Adapter<NotificationsRecyclerAdapter.NotificationViewHolder>(){

    private var notificationsList = listOf<DonationNotification>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(LayoutInflater.from(parent.context).
        inflate(R.layout.item_notification, parent, false))
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bindRequestData(notificationsList[position])
    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    fun setNotificationsList(notifications: List<DonationNotification>){
        this.notificationsList = notifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindRequestData(notification: DonationNotification){
            itemView.notificationTitle.text = notification.title
            itemView.notificationDetails.text = notification.details
            itemView.notificationTimeElapsed.text = "${itemView.notificationTimeElapsed.text} " +
                    "${notification.dateTime}"
            itemView.newNotificationIcon.visibility = if(notification.isRead) View.INVISIBLE else View.VISIBLE
        }
    }
}