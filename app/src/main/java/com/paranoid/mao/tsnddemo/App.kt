package com.paranoid.mao.tsnddemo

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * Created by Paranoid on 1/31/18.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }
}