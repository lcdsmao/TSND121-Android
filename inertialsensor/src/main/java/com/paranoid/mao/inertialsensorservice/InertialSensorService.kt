package com.paranoid.mao.inertialsensorservice

import io.reactivex.Completable
import io.reactivex.Flowable

interface InertialSensorService {

    val status: Flowable<InertialSensorStatus>
    val sensorData: Flowable<InertialSensorData>

    fun connect()
    fun disconnect()
    fun startMeasure()
    fun stopMeasure()
}