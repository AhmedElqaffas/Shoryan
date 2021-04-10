package com.example.shoryan.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.TokenResponse
import com.example.shoryan.data.Tokens
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.TokensRefresher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TokensViewModel @Inject constructor (private val bloodDonationAPI: RetrofitBloodDonationInterface)
    : ViewModel() {
    suspend fun getNewAccessToken(context: Context): TokenResponse =
         withContext(viewModelScope.coroutineContext) {
             TokensRefresher.getNewAccessToken(bloodDonationAPI, context)
         }

    suspend fun saveTokens(tokens: Tokens, context: Context){
        TokensRefresher.saveTokens(tokens.accessToken, tokens.refreshToken, context)
    }
}