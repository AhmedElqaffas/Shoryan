package com.example.shoryan.di

import com.example.shoryan.ui.RequestDetailsFragment
import com.example.shoryan.ui.SplashScreenFragment
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@ActivityScope
@Subcomponent
interface SplashScreenComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): SplashScreenComponent
    }

    fun inject(fragment: SplashScreenFragment)
}