package com.example.shoryan.ui.recyclersAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.DonationNotification
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.databinding.ItemNotificationBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.viewmodels.NotificationsViewModel

class NotificationsRecyclerAdapter(private val requestsRecyclerInteraction: RequestsRecyclerInteraction,
private val notificationsViewModel: NotificationsViewModel,
private val lifecycleOwner: LifecycleOwner):
    ListAdapter<DonationNotification, NotificationsRecyclerAdapter.NotificationViewHolder>(NotificationsRecyclerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NotificationViewHolder(ItemNotificationBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bindRequestData(getItem(position))
    }

    inner class NotificationViewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root){

        init{
            setClickListener()
        }

        private fun setClickListener(){
            itemView.setOnClickListener{
                val notification = getItem(layoutPosition)
                requestsRecyclerInteraction.onRequestCardClicked(notification.request, isMyRequest(notification.request))
            }
        }

        private fun isMyRequest(item: DonationRequest) = item.requester?.id == CurrentSession.user?.id

        fun bindRequestData(notification: DonationNotification){
            binding.notification = notification
            binding.viewmodel = notificationsViewModel
            binding.lifecycleOwner = lifecycleOwner
        }
    }
}

class NotificationsRecyclerDiffCallback: DiffUtil.ItemCallback<DonationNotification>(){
    override fun areItemsTheSame(oldItem: DonationNotification, newItem: DonationNotification): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DonationNotification, newItem: DonationNotification): Boolean {
        return oldItem == newItem
    }

}