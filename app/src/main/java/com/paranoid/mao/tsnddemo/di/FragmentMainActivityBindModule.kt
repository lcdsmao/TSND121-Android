package com.paranoid.mao.tsnddemo.di

import com.paranoid.mao.tsnddemo.ui.main.SensorControlFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentMainActivityBindModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun sensorControlFragmentInjector(): SensorControlFragment
}