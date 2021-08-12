package com.example.shoryan.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shoryan.repos.TokensRefresher
import java.util.*

class SplashScreenViewModel: ViewModel() {

    private val _currentLocale = MutableLiveData(Locale.ENGLISH)
    val currentLocale: LiveData<Locale> = _currentLocale
    // Used to display the animation only when the fragment is first created, otherwise skip animation
    var firstTimeOpened = true

    suspend fun retrieveTokensFromDataStore(context: Context) {
        TokensRefresher.retrieveTokensFromDataStore(context)
    }

    fun updateLocale(languageTag: String){
        val newLocale = Locale.forLanguageTag(languageTag)
        _currentLocale.value = newLocale
    }
}