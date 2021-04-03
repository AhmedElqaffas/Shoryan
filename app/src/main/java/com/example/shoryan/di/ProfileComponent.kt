package com.example.shoryan.di

import com.example.shoryan.ui.ProfileFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface ProfileComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): ProfileComponent
    }

    fun inject(fragment: ProfileFragment)
}