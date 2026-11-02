package com.example.lifetrack

import android.app.Application
import com.example.lifetrack.di.AppContainer

class LifeTrackApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
