package com.example.shoryan.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.shoryan.repos.TokensRefresher

class SplashScreenViewModel: ViewModel() {

    suspend fun retrieveTokensFromDataStore(context: Context) {
        TokensRefresher.retrieveTokensFromDataStore(context)
    }
}