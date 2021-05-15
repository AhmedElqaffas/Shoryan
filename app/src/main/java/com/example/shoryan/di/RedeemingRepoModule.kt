package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import com.example.shoryan.repos.RewardsRepo_imp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@DisableInstallInCheck // Keep it while dagger is still used, remove it when migration is complete
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