package com.example.shoryan.di

import com.example.shoryan.ui.PasswordLoginFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface LoginComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginComponent
    }

    // Classes that can be injected by this Component
    fun inject(fragment: PasswordLoginFragment)
}