package com.paranoid.mao.inertialsensorservice

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

interface InertialSensorService {

    enum class ServiceStatus {
        IDLE,
        COMMAND,
        ONLINE_MEASURE,
    }

    fun connect(): Flowable<InertialSensorResponse>
    fun disconnect()
    fun startMeasure(): Flowable<InertialSensorData>
    fun stopMeasure()
}