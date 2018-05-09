package com.paranoid.mao.tsnddemo.di

import com.paranoid.mao.tsnddemo.ui.manage.SensorManageFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentManageActivityBindModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun sensorManageFragmentInjector(): SensorManageFragment
}