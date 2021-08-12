package com.example.shoryan.ui.recyclersAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.RequesterType
import com.example.shoryan.databinding.ItemRequestBankBinding
import com.example.shoryan.databinding.ItemRequestUserBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction

class RequestsRecyclerAdapter(private val requestsRecyclerInteraction: RequestsRecyclerInteraction):
    ListAdapter<DonationRequest, RecyclerView.ViewHolder>(RequestsRecyclerDiffCallback()) {

    private val USER_REQUEST = 0
    private val BLOOD_BANK_REQUEST = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            USER_REQUEST -> UserRequestViewHolder(ItemRequestUserBinding.inflate(inflater, parent, false))
            else -> BloodBankRequestViewHolder(ItemRequestBankBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            USER_REQUEST -> ( holder as UserRequestViewHolder).bindRequestData(getItem(position))
            BLOOD_BANK_REQUEST -> ( holder as BloodBankRequestViewHolder).bindRequestData(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(this.getItem(position).requesterType){
            RequesterType.User -> USER_REQUEST
            RequesterType.RegisteredBloodBank -> BLOOD_BANK_REQUEST
            null -> USER_REQUEST
        }
    }

    inner class UserRequestViewHolder(val binding: ItemRequestUserBinding): RecyclerView.ViewHolder(binding.root){

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

    inner class BloodBankRequestViewHolder(val binding: ItemRequestBankBinding): RecyclerView.ViewHolder(binding.root){

        init{
            setClickListener()
        }

        private fun setClickListener(){
            itemView.setOnClickListener{
                val request = getItem(layoutPosition)
                requestsRecyclerInteraction.onRequestCardClicked(request, false)
            }
        }

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