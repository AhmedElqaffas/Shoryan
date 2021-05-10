package com.example.shoryan.di

import com.example.shoryan.networking.RetrofitBloodDonationInterface
import dagger.Component
import javax.inject.Singleton

@Singleton
@ApplicationScope
@Component(modules = [RetrofitModule::class, AppSubComponents::class])
interface AppComponent {

    fun splashScreenComponent(): SplashScreenComponent.Factory
    fun loginComponent(): LoginComponent.Factory
    fun homeComponent(): HomeComponent.Factory
    fun smsComponent(): SMSComponent.Factory
    fun profileComponent(): ProfileComponent.Factory
    fun registrationComponent(): RegistrationComponent.Factory
    fun requestDetailsComponent(): RequestDetailsComponent.Factory
    fun myRequestsComponent(): MyRequestsComponent.Factory
    fun newRequestComponent(): NewRequestComponent.Factory
    fun rewardsComponent(): RewardsComponent.Factory
    fun redeemRewardComponent(): RedeemRewardComponent.Factory
    fun getApiInterface(): RetrofitBloodDonationInterface
}