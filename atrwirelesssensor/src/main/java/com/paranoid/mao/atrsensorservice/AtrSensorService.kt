package com.paranoid.mao.atrsensorservice

import io.reactivex.Flowable

interface AtrSensorService {

    val status: Flowable<AtrSensorStatus>
    val sensorData: Flowable<AccGyroMagData>

    fun connect()
    fun disconnect()
    fun startMeasure()
    fun stopMeasure()
}