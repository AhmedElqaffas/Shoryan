package com.example.shoryan.di

import com.example.shoryan.ui.RegistrationFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface RegistrationComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): RegistrationComponent
    }

    fun inject(fragment: RegistrationFragment)
}