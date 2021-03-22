package com.example.shoryan.di

import com.example.shoryan.ui.RequestDetailsFragment
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@ActivityScope
@Subcomponent
interface RequestDetailsComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(@BindsInstance @Named("requestId") requestId: String): RequestDetailsComponent
    }

    fun inject(fragment: RequestDetailsFragment)
}