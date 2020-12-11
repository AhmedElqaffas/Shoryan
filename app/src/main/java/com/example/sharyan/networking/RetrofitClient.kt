package com.example.sharyan.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofitClient: Retrofit? = null

    fun getRetrofitClient(): Retrofit{
        if (retrofitClient == null) {
            retrofitClient = Retrofit
                .Builder()
                .baseUrl("https://pacific-springs-77989.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
        }
        return retrofitClient!!
    }
}