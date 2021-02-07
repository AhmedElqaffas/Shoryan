package com.example.shoryan.networking

import androidx.paging.PagingSource
import com.example.shoryan.data.DonationRequest

class RequestsPagingSource(
    private val bloodDonationAPI: RetrofitBloodDonationInterface,
    private val query: String
    ): PagingSource<Int, DonationRequest>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DonationRequest> {
        TODO("Not yet implemented")
    }
}