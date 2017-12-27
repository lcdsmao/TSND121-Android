package com.paranoid.mao.tsnddemo.tsnd

/**
 * Created by Paranoid on 12/20/17.
 */
data class SensorData(val time: Int = 0,
                      val accX: Int = 0, val accY: Int = 0, val accZ: Int = 0,
                      val gyroX: Int = 0, val gyroY: Int = 0, val gyroZ: Int = 0,
                      val magX: Int = 0, val magY: Int = 0, val magZ: Int = 0) {
    fun toList() = listOf<Int>(time, accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magX, magZ)
}