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

    /**
     * Used to generate new tokens when the access token has expired. The old refresh token is
     * sent to the server, and a new pair of access and refresh tokens is retrieved.
     * @param context The current context, used to store the new tokens in a local dataStore
     * @return TokenResponse
     */
    suspend fun getNewAccessToken(context: Context): TokenResponse =
         withContext(viewModelScope.coroutineContext) {
             TokensRefresher.getNewAccessToken(bloodDonationAPI, context)
         }

    /**
     * Saves a pair of access and refresh tokens in the device dataStore
     * @param tokens Object containing the access token and refresh tokens to be stored
     * @param context The current context, used to store the new tokens in a local dataStore
     * @return TokenResponse
     */
    suspend fun saveTokens(tokens: Tokens, context: Context){
        TokensRefresher.saveTokens(tokens.accessToken, tokens.refreshToken, context)
    }
}