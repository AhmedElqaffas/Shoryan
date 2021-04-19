package com.example.shoryan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")

object DataStoreUtil {

    const val ACCESS_TOKEN_KEY = "accessToken"
    const val REFRESH_TOKEN_KEY = "refreshToken"
    const val LANGUAGE = "language"

    suspend fun Context.write(key: String, value: String) {
        dataStore.edit { settings ->
            settings[stringPreferencesKey(key)] = value
        }
    }

    suspend fun Context.read(key: String, defaultValue: String?): String {
        return dataStore.data.map { settings ->
            settings[stringPreferencesKey(key)] ?: defaultValue
        }.first().toString()
    }

    suspend fun Context.clearTokens(){
        dataStore.edit {
            it.remove(stringPreferencesKey(REFRESH_TOKEN_KEY))
            it.remove(stringPreferencesKey(ACCESS_TOKEN_KEY))
        }
    }
}