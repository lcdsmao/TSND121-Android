package com.paranoid.mao.tsnddemo.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Paranoid on 1/25/18.
 */
@Entity
data class Sensor(
        val name: String,
        val mac: String,
        @ColumnInfo(name = "enable_status") val enableStatus: Boolean = false,
        @PrimaryKey(autoGenerate = true) val id: Int = 0
)