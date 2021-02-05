package com.example.pozterminal3

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Show.init(this)
    }
}