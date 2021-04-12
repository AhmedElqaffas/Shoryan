package com.example.shoryan.repos

import android.content.Context
import android.util.Log
import com.example.shoryan.DataStoreUtil
import com.example.shoryan.DataStoreUtil.clear
import com.example.shoryan.DataStoreUtil.read
import com.example.shoryan.DataStoreUtil.write
import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.data.TokenResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface

object TokensRefresher {

    var accessToken: String? = null
    var refreshToken: String? = null

    /**
     * Used to generate new tokens when the access token has expired. The old refresh token is
     * sent to the server, and a new pair of access and refresh tokens is retrieved.
     * @param bloodDonationAPI The retrofit interface containing the server endpoints
     * @param context The current context, used to store the new tokens in a local dataStore
     * @return TokenResponse
     */
    suspend fun getNewAccessToken(bloodDonationAPI: RetrofitBloodDonationInterface,
                                  context: Context): TokenResponse{
        return try {
            val response = bloodDonationAPI.getNewAccessToken(refreshToken!!)
            processResponse(response, context)
            response
        }
        catch (e: Exception){
            Log.e("GetNewAccessTokenRepo","${e.stackTrace}")
            clearCachedTokens(context)
            TokenResponse(null,null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    private suspend fun processResponse(response: TokenResponse, context: Context){
        // If access token isn't null, then refresh token is also not null
        if(response.accessToken != null){
            saveTokens(response.accessToken, response.refreshToken!!, context)
        }else{
            // Refreshing tokens failed, user should be logged out and tokens should be cleared
            // Here we will only handle clreading the tokens
            this.accessToken = null
            this.refreshToken = null
            clearCachedTokens(context)
        }
    }

    private suspend fun saveTokenInDevice(key: String, token: String, context: Context){
        context.write(key, token)
    }

    suspend fun retrieveTokensFromDataStore(context: Context) {
        accessToken = context.read(DataStoreUtil.ACCESS_TOKEN_KEY, "")
        refreshToken = context.read(DataStoreUtil.REFRESH_TOKEN_KEY, "")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, context: Context){
        this.accessToken = "Bearer $accessToken"
        this.refreshToken = refreshToken
        saveTokenInDevice(DataStoreUtil.ACCESS_TOKEN_KEY, this.accessToken!!, context)
        saveTokenInDevice(DataStoreUtil.REFRESH_TOKEN_KEY, refreshToken, context)
    }

    suspend fun clearCachedTokens(context: Context){
        context.clear()
    }
}