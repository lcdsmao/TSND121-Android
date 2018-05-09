package com.paranoid.mao.tsnddemo.data

import android.content.SharedPreferences
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
        private val database: AppDatabase,
        private val sharedPreferences: SharedPreferences) {

    val sensorList = database.sensorDao()
            .getAll()
            .subscribeOn(Schedulers.io())

    val sensorEnabledList = database.sensorDao()
            .getEnabled()
            .subscribeOn(Schedulers.io())

    fun insert(vararg sensor: Sensor) = database.sensorDao().insert(*sensor)

    fun delete(sensor: Sensor) = database.sensorDao().delete(sensor)
}