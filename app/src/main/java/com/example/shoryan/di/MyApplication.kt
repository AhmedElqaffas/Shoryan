package com.example.shoryan.di

import android.app.Application

open class MyApplication: Application() {
    val appComponent: AppComponent = DaggerAppComponent.create()
}