package com.example.shoryan.di

import com.example.shoryan.ui.HomeFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface HomeComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): HomeComponent
    }

    fun inject(fragment: HomeFragment)
}