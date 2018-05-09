package com.paranoid.mao.tsnddemo.di

import com.paranoid.mao.tsnddemo.ui.graph.GraphActivity
import com.paranoid.mao.tsnddemo.ui.main.MainActivity
import com.paranoid.mao.tsnddemo.ui.manage.ManageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBindModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentMainActivityBindModule::class])
    abstract fun mainActivityInjector(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentManageActivityBindModule::class])
    abstract fun manageActivityInjector(): ManageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentGraphActivityBindModule::class])
    abstract fun graphActivityInjector(): GraphActivity
}