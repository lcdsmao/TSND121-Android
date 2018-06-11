package com.paranoid.mao.tsnd121

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import org.koin.android.ext.android.startKoin

/**
 * Created by Paranoid on 1/31/18.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(module))
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }
}