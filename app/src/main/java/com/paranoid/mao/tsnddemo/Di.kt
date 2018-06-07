package com.paranoid.mao.tsnddemo

import android.arch.persistence.room.Room
import com.paranoid.mao.tsnddemo.data.database.AppDatabase
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.ui.graph.GraphViewModel
import com.paranoid.mao.tsnddemo.ui.main.SensorControlViewModel
import com.paranoid.mao.tsnddemo.ui.manage.ManageViewModel
import org.jetbrains.anko.defaultSharedPreferences
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext

val module: Module = applicationContext {
    bean { Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "database").build() }
    bean { androidApplication().defaultSharedPreferences }
    bean { DataRepository(get(), get()) }
    viewModel { SensorControlViewModel(get()) }
    viewModel { ManageViewModel(get()) }
    viewModel { GraphViewModel(get()) }
}