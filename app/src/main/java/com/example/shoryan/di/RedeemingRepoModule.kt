package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import com.example.shoryan.repos.RewardsRepo_imp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RedeemingRepoModule {

    @Provides
    @Singleton
    fun provideRepo(
        retrofit: RetrofitBloodDonationInterface
    ): RewardsRepo {
        return RewardsRepo_imp(retrofit)
    }

}