package com.paranoid.mao.tsnddemo.di

import com.paranoid.mao.tsnddemo.ui.graph.RealtimeGraphFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentGraphActivityBindModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun realtimeGraphFragmentInjector(): RealtimeGraphFragment
}