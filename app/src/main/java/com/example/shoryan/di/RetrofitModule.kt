package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule{
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): RetrofitBloodDonationInterface{
        return  Retrofit.Builder().baseUrl("https://pacific-springs-77989.herokuapp.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(RetrofitBloodDonationInterface::class.java)
    }

    @Provides
    @Singleton
    fun getOkHttpClient() = OkHttpClient.Builder()
        .build()
}