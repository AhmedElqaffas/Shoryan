package com.example.shoryan.di

import dagger.Module

@Module(subcomponents = [
    SplashScreenComponent::class,
    LoginComponent::class,
    RegistrationComponent::class,
    RequestDetailsComponent::class,
    MyRequestsComponent::class,
    ProfileComponent::class
])
class AppSubComponents