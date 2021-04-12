package com.example.shoryan.di

import com.example.shoryan.ui.NewRequestFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface NewRequestComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): NewRequestComponent
    }

    fun inject(fragment: NewRequestFragment)
}