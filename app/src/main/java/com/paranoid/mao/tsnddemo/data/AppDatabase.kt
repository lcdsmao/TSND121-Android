package com.paranoid.mao.tsnddemo.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.paranoid.mao.tsnddemo.vo.Sensor

@Database(entities = [Sensor::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDao() : SensorDao
}