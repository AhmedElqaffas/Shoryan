package com.example.shoryan.di

import com.example.shoryan.ui.RewardsFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface RewardsComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(): RewardsComponent
    }

    fun inject(fragment: RewardsFragment)
}