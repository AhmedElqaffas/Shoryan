package com.example.shoryan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.AccessController.getContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")

object DataStoreUtil {

    val ACCESS_TOKEN_KEY = "accessToken"

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

    suspend fun Context.clear(context: Context){
        dataStore.edit { it.clear() }
    }
}