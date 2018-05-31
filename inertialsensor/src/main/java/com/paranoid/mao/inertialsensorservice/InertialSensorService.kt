package com.paranoid.mao.inertialsensorservice

import io.reactivex.Completable
import io.reactivex.Flowable

interface InertialSensorService {
    fun connect(): Flowable<InertialSensorData>
    fun disconnect(): Completable
    fun startMeasure(): Completable
    fun stopMeasure(): Completable
}