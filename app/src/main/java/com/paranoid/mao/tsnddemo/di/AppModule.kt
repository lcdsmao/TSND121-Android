package com.paranoid.mao.tsnddemo.di

import android.app.Application
import android.arch.persistence.room.Room
import com.paranoid.mao.tsnddemo.data.AppDatabase
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideApplication() = application

    @Singleton
    @Provides
    fun provideDatabase() = Room.databaseBuilder(
            application, AppDatabase::class.java, "database").build()

    @Singleton
    @Provides
    fun providePreference() = application.defaultSharedPreferences
}