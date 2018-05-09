package com.paranoid.mao.tsnddemo.vo

sealed class SensorResponse(open val sensor: Sensor) {
    data class Connected(override val sensor: Sensor) : SensorResponse(sensor)
    data class Disconnected(override val sensor: Sensor) : SensorResponse(sensor)
    data class MeasureStarted(override val sensor: Sensor) : SensorResponse(sensor)
    data class MeasureStopped(override val sensor: Sensor) : SensorResponse(sensor)
    data class Failed(override val sensor: Sensor) : SensorResponse(sensor)
}