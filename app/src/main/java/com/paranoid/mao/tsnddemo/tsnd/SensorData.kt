package com.paranoid.mao.tsnddemo.tsnd

/**
 * Created by Paranoid on 12/20/17.
 */
data class SensorData(val time: Int,
                      val accX: Int, val accY: Int, val accZ: Int,
                      val gyroX: Int, val gyroY: Int, val gyroZ: Int,
                      val magX: Int = 0, val magY: Int = 0, val magZ: Int = 0) {
}