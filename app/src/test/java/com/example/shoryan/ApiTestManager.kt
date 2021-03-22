package com.example.shoryan

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiTestManager(val url: String) {

    private var retrofitClient: Retrofit? = null

    fun getRetrofitClient(): Retrofit{
        if (retrofitClient == null) {
            retrofitClient = Retrofit
                .Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
        }
        return retrofitClient!!
    }

    val api = getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
}