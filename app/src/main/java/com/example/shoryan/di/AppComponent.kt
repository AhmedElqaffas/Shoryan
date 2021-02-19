package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.ui.RequestDetailsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@ApplicationScope
@Component(modules = [RetrofitModule::class])
interface AppComponent {

    fun getApiInterface(): RetrofitBloodDonationInterface
    fun inject(fragment: RequestDetailsFragment)
}