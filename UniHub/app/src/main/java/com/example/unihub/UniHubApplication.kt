package com.example.unihub

import android.app.Application
import com.example.unihub.data.config.TokenManager

class UniHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.initialize(this)
    }
}