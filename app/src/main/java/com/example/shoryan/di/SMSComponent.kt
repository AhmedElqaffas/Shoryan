package com.example.shoryan.di

import com.example.shoryan.ui.PasswordLoginFragment
import com.example.shoryan.ui.SMSFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface SMSComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SMSComponent
    }

    // Classes that can be injected by this Component
    fun inject(fragment: SMSFragment)
}