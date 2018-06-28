package com.paranoid.mao.atrsensorservice

import io.reactivex.Flowable

interface AtrSensorService {

    val status: Flowable<AtrSensorStatus>
    val accGyrData: Flowable<AccGyrData>
    val magData: Flowable<MagData>

    fun connect()
    fun disconnect()
    fun startMeasure()
    fun stopMeasure()
}