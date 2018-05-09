package com.paranoid.mao.tsnddemo

import android.app.Activity
import android.app.Application
import com.paranoid.mao.tsnddemo.di.AppModule
import com.paranoid.mao.tsnddemo.di.DaggerAppComponent
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * Created by Paranoid on 1/31/18.
 */
class App : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingActivityInjector : DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
        DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
                .inject(this)

    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector
}