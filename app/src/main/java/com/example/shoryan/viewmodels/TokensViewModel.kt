package com.example.shoryan.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.TokenRefreshResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.TokensRefresher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TokensViewModel @Inject constructor (private val bloodDonationAPI: RetrofitBloodDonationInterface)
    : ViewModel() {
      suspend fun getNewAccessToken(context: Context): TokenRefreshResponse =
         withContext(viewModelScope.coroutineContext) {
             TokensRefresher.getNewAccessToken(bloodDonationAPI, context)
         }
}