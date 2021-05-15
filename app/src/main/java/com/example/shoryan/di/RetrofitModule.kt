package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@DisableInstallInCheck
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

@DisableInstallInCheck // Keep it while dagger is still used, remove it when migration is complete
@Module
@InstallIn(SingletonComponent::class)
object HiltNetworkModule{
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