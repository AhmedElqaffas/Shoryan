package com.example.shoryan.di

import dagger.Module

@Module(subcomponents = [
    LoginComponent::class,
    RegistrationComponent::class,
    RequestDetailsComponent::class
])
class AppSubComponents