package com.paranoid.mao.tsnd121

import android.arch.persistence.room.Room
import android.os.Environment
import com.paranoid.mao.tsnd121.data.database.AppDatabase
import com.paranoid.mao.tsnd121.data.DataRepository
import com.paranoid.mao.tsnd121.ui.graph.GraphViewModel
import com.paranoid.mao.tsnd121.ui.main.SensorControlViewModel
import com.paranoid.mao.tsnd121.ui.manage.ManageViewModel
import org.jetbrains.anko.defaultSharedPreferences
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext

val module: Module = applicationContext {
    bean { Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "database").build() }
    bean { androidApplication().defaultSharedPreferences }
    bean { DataRepository(get(), get(), get("rootFolder")) }
    bean(name = "rootFolder") { "${Environment.getExternalStorageDirectory()}/TSND121" }
    viewModel { SensorControlViewModel(get()) }
    viewModel { ManageViewModel(get()) }
    viewModel { GraphViewModel(get()) }
}