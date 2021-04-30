package com.example.shoryan.ui.recyclersAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.databinding.ItemRequestBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction

class RequestsRecyclerAdapter(private val requestsRecyclerInteraction: RequestsRecyclerInteraction):
    ListAdapter<DonationRequest, RequestsRecyclerAdapter.RequestViewHolder>(RequestsRecyclerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRequestBinding.inflate(inflater, parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bindRequestData(getItem(position))
    }

    inner class RequestViewHolder(val binding: ItemRequestBinding): RecyclerView.ViewHolder(binding.root){

        init{
            setClickListener()
        }

        private fun setClickListener(){
            itemView.setOnClickListener{
                val request = getItem(layoutPosition)
                requestsRecyclerInteraction.onRequestCardClicked(request, isMyRequest(request))
            }
        }

        private fun isMyRequest(item: DonationRequest) = item.requester?.id == CurrentSession.user?.id

        fun bindRequestData(request: DonationRequest){
            binding.item = request
        }
    }
}

class RequestsRecyclerDiffCallback: DiffUtil.ItemCallback<DonationRequest>(){
    override fun areItemsTheSame(oldItem: DonationRequest, newItem: DonationRequest): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DonationRequest, newItem: DonationRequest): Boolean {
        return oldItem == newItem
    }

}