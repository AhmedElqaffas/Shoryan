package com.example.sharyan.recyclersAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import kotlinx.android.synthetic.main.item_request.view.*

class RequestsRecyclerAdapter: RecyclerView.Adapter<RequestsRecyclerAdapter.RequestViewHolder>() {

    private var requestsList = mutableListOf<DonationRequest>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        return RequestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false))
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bindRequestData(requestsList[position])
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }

    fun addRequests(requests: List<DonationRequest>){
        requestsList.addAll(requests)
    }

    inner class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindRequestData(request: DonationRequest){
            itemView.request_item_blood_type.text = request.bloodType
            itemView.request_item_name.text = request.requesterName
            itemView.request_item_location.text = request.location
        }
    }
}