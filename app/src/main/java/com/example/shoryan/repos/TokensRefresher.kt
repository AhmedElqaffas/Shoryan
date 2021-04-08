package com.example.shoryan.repos

import android.content.Context
import com.example.shoryan.DataStoreUtil
import com.example.shoryan.DataStoreUtil.clear
import com.example.shoryan.DataStoreUtil.read
import com.example.shoryan.DataStoreUtil.write
import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.data.TokenRefreshResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface

object TokensRefresher {

    var accessToken: String? = null
    var refreshToken: String? = null

    suspend fun getNewAccessToken(bloodDonationAPI: RetrofitBloodDonationInterface,
                                  context: Context): TokenRefreshResponse{
        return try {
            val response = bloodDonationAPI.getNewAccessToken(refreshToken!!)
            processResponse(response, context)
            response
        }
        catch (e: Exception){
            clearCachedTokens(context)
            TokenRefreshResponse(null,null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    private suspend fun processResponse(response: TokenRefreshResponse, context: Context){
        if(response.accessToken != null){
            saveTokens(response.accessToken, response.refreshToken!!, context)
        }else{
            // Refreshing token failed, user should be logged out and tokens should be cleared
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