package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.ui.RequestDetailsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@ApplicationScope
@Component(modules = [RetrofitModule::class, AppSubComponents::class])
interface AppComponent {

    fun loginComponent(): LoginComponent.Factory
    fun getApiInterface(): RetrofitBloodDonationInterface

    fun inject(fragment: RequestDetailsFragment)
}