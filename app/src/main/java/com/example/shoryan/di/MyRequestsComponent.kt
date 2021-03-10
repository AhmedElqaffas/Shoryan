package com.example.shoryan.di

import com.example.shoryan.ui.MyRequestsFragment
import com.example.shoryan.ui.PasswordLoginFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface MyRequestsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MyRequestsComponent
    }

    // Classes that can be injected by this Component
    fun inject(fragment: MyRequestsFragment)
}