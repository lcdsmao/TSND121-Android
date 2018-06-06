package com.paranoid.mao.tsnddemo.vo

sealed class SensorResponse(open val sensor: Sensor) {
    data class Command(override val sensor: Sensor) : SensorResponse(sensor)
    data class Measuring(override val sensor: Sensor) : SensorResponse(sensor)
    data class Offline(override val sensor: Sensor) : SensorResponse(sensor)
}