package com.propodcast.app.android

import android.app.Application
import com.propodcast.app.core.crash.setUpAndroidLogging
import com.propodcast.app.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class ProPodcastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setUpAndroidLogging(this, debug = BuildConfig.DEBUG)
        initKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            androidContext(this@ProPodcastApplication)
        }
    }
}
