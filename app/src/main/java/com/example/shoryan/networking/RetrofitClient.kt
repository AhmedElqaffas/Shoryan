package com.example.shoryan.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofitClient: Retrofit? = null

    fun getRetrofitClient(): Retrofit{
        if (retrofitClient == null) {
            retrofitClient = Retrofit
                .Builder()
                .baseUrl("https://shoryan.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
        }
        return retrofitClient!!
    }
}