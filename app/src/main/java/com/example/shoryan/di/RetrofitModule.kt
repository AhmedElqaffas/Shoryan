package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RetrofitModule {

    @Provides
    @ApplicationScope
    fun getApiInterface(retrofit: Retrofit): RetrofitBloodDonationInterface {
        return retrofit.create(RetrofitBloodDonationInterface::class.java)
    }

    @Provides
    @ApplicationScope
    fun getRetrofitClient(okHttpClient: OkHttpClient) = Retrofit.Builder()
        .baseUrl("https://pacific-springs-77989.herokuapp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    @Provides
    @ApplicationScope
    fun getOkHttpClient() = OkHttpClient.Builder()
        .build()

}