package com.anshyeon.onoff

import android.app.Application
import com.anshyeon.onoff.data.PreferenceManager

class OnOffApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferenceManager(this)
        appContainer = AppContainer()
    }

    companion object {
        lateinit var preferencesManager: PreferenceManager
        lateinit var appContainer: AppContainer
    }
}