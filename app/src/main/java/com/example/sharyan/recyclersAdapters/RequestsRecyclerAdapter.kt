package com.example.sharyan.recyclersAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import kotlinx.android.synthetic.main.item_request.view.*

class RequestsRecyclerAdapter(private val requestsRecyclerInteraction: RequestsRecyclerInteraction):
    ListAdapter<DonationRequest, RequestsRecyclerAdapter.RequestViewHolder>(RequestsRecyclerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        return RequestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false))
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bindRequestData(getItem(position))
    }

    inner class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        init{
            setClickListener()
        }

        private fun setClickListener(){
            itemView.setOnClickListener{
                requestsRecyclerInteraction.onItemClicked(getItem(layoutPosition))
            }
        }

        fun bindRequestData(request: DonationRequest){
            itemView.request_item_blood_type.text = request.bloodType
            itemView.request_item_name.text = request.requester.name.firstName
            itemView.request_item_location.text = request.donationLocation.region
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