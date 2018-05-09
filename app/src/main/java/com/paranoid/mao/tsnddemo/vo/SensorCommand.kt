package com.paranoid.mao.tsnddemo.vo

/**
 * Created by Paranoid on 1/30/18.
 */
sealed class SensorCommand(open val sensor: Sensor) {
    data class Connect(override val sensor: Sensor) : SensorCommand(sensor)
    data class Disconnect(override val sensor: Sensor) : SensorCommand(sensor)
    data class StartMeasure(override val sensor: Sensor) : SensorCommand(sensor)
    data class StopMeasure(override val sensor: Sensor) : SensorCommand(sensor)
}