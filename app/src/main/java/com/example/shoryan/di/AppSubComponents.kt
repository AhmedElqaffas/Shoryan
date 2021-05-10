package com.example.shoryan.di

import dagger.Module

@Module(subcomponents = [
    SplashScreenComponent::class,
    LoginComponent::class,
    HomeComponent::class,
    SMSComponent::class,
    RegistrationComponent::class,
    RequestDetailsComponent::class,
    MyRequestsComponent::class,
    NewRequestComponent::class,
    ProfileComponent::class,
    RewardsComponent::class,
    RedeemRewardComponent::class
])
class AppSubComponents