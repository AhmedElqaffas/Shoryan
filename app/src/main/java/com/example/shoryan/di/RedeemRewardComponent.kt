package com.example.shoryan.di

import com.example.shoryan.ui.RedeemRewardFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface RedeemRewardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): RedeemRewardComponent
    }

    // Classes that can be injected by this Component
    fun inject(fragment: RedeemRewardFragment)
}