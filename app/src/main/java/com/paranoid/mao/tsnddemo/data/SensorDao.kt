package com.paranoid.mao.tsnddemo.data

import android.arch.persistence.room.*
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.Flowable

@Dao
interface SensorDao {

    @Query("SELECT * FROM sensor")
    fun getAll(): Flowable<List<Sensor>>

    @Query("SELECT * FROM sensor")
    fun getAllTest(): List<Sensor>

    @Query("SELECT * FROM sensor WHERE enable_status > 0")
    fun getEnabled(): Flowable<List<Sensor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg info: Sensor)

    @Delete
    fun delete(info: Sensor)
}