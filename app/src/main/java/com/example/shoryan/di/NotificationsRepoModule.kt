package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.NotificationsRepo
import com.example.shoryan.repos.NotificationsRepo_imp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsRepoModule {

    @Provides
    @Singleton
    fun provideRepo(
        retrofit: RetrofitBloodDonationInterface
    ): NotificationsRepo {
        return NotificationsRepo_imp(retrofit)
    }

}