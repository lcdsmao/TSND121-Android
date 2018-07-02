package com.paranoid.mao.tsnd121.vo

data class AccGyrMagData(val time: Int = 0,
                          val accX: Int = 0, val accY: Int = 0, val accZ: Int = 0,
                          val gyrX: Int = 0, val gyrY: Int = 0, val gyrZ: Int = 0,
                          val magX: Int = 0, val magY: Int = 0, val magZ: Int = 0) {
    fun toList() = listOf(time, accX, accY, accZ, gyrX, gyrY, gyrZ, magX, magY, magZ)
}