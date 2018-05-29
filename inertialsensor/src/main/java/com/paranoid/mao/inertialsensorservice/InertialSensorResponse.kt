package com.paranoid.mao.inertialsensorservice

sealed class InertialSensorResponse(val message: String? = null)

object StartMeasure : InertialSensorResponse()
object StopMeasure : InertialSensorResponse()
class SensorStatus(message: String) : InertialSensorResponse(message)
class BatteryStatus(message: String) : InertialSensorResponse(message)
class Error(message: String) : InertialSensorResponse(message)
