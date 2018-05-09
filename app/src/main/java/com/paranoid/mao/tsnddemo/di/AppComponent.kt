package com.paranoid.mao.tsnddemo.di

import com.paranoid.mao.tsnddemo.App
import com.paranoid.mao.tsnddemo.di.ActivityBindModule
import com.paranoid.mao.tsnddemo.di.AppModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AndroidInjectionModule::class, ActivityBindModule::class])
interface AppComponent {

    fun inject(app: App)
}