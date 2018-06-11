package com.paranoid.mao.tsnd121.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.paranoid.mao.tsnd121.vo.Sensor

@Database(entities = [Sensor::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDao() : SensorDao
}