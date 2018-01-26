package com.paranoid.mao.tsnddemo.db

import android.content.Context
import com.paranoid.mao.tsnddemo.model.SensorInfo
import org.jetbrains.anko.db.*

/**
 * Created by Paranoid on 1/25/18.
 */
class DbManager(private val ctx: Context) {

    fun loadSensorInfo(): List<SensorInfo> {
        val sensorInfoList = ctx.database.use {
            select(DbEntry.SENSOR_DATABASE)
                    .parseList(classParser<SensorInfo>())
        }
        return sensorInfoList
    }

    fun loadEnabledSensorInfo(): List<SensorInfo> {
        val sensorInfoList = ctx.database.use {
            select(DbEntry.SENSOR_DATABASE)
                    .whereArgs("${DbEntry.SENSOR_STATUS} > 0")
                    .parseList(classParser<SensorInfo>())
        }
        return sensorInfoList
    }

    fun update(info: SensorInfo): Int {
        if (isIllegalMAC(info.mac)) return -1
        val num = ctx.database.use {
            update(DbEntry.SENSOR_DATABASE,
                    DbEntry.SENSOR_NAME to info.name,
                    DbEntry.SENSOR_MAC to info.mac,
                    DbEntry.SENSOR_STATUS to info.status)
                    .whereArgs("${DbEntry.SENSOR_ID} = ${info.id}")
                    .exec()
        }
        return num
    }

    fun delete(info: SensorInfo): Int {
        val num = ctx.database.use {
            delete(DbEntry.SENSOR_DATABASE,
                    "",
                    DbEntry.SENSOR_ID to info.id)
        }
        return num
    }

    fun insert(info: SensorInfo): Int {
        if (isIllegalMAC(info.mac)) return -1
        val id = ctx.database.use {
            insert(DbEntry.SENSOR_DATABASE,
                    DbEntry.SENSOR_NAME to info.name,
                    DbEntry.SENSOR_MAC to info.mac,
                    DbEntry.SENSOR_STATUS to info.status)
        }
        return id.toInt()
    }

    private fun isIllegalMAC(mac: String): Boolean =
            mac.matches(Regex("^([0-9A-F]{2}:){5}([0-9A-F]{2})$"))
}