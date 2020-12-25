package com.example.sharyan.networking

import androidx.paging.PagingSource
import com.example.sharyan.data.DonationRequest

class RequestsPagingSource(
    private val bloodDonationAPI: RetrofitBloodDonationInterface,
    private val query: String
    ): PagingSource<Int, DonationRequest>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DonationRequest> {
        TODO("Not yet implemented")
    }
}