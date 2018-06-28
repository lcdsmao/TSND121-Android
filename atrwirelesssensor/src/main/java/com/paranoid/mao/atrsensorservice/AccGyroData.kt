package com.paranoid.mao.atrsensorservice

data class AccGyroData(
        val time: Int = 0,
        val accX: Int = 0,
        val accY: Int = 0,
        val accZ: Int = 0,
        val gyroX: Int = 0,
        val gyroY: Int = 0,
        val gyroZ: Int = 0
)
