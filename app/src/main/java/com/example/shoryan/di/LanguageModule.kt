package com.example.shoryan.di

import com.example.shoryan.LocaleHelper
import com.example.shoryan.data.Language
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {

    @Provides
    fun provideLanguage(): Language {
        return if(LocaleHelper.currentLanguage == LocaleHelper.LANGUAGE_EN){
            Language.english
        }else{
            Language.arabic
        }
    }

}