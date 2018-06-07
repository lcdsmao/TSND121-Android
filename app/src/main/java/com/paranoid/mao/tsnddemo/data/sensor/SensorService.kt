package com.paranoid.mao.tsnddemo.data.sensor

import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.atrsensorservice.tsnd121.Tsnd121Service
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorService(private val sensor: Sensor,
                    var isSave: Boolean = false,
                    private val statusMap: MutableMap<Sensor, AtrSensorStatus>,
                    private val statusStream: PublishProcessor<Pair<Sensor, AtrSensorStatus>>) {

    private val service = Tsnd121Service(sensor.name, sensor.mac, 10)
    private var sensorDataSaver: SensorDataSaver? = null
    private var sensorDataDisposable: Disposable? = null
    private var sensorStatusDisposable: Disposable? = null
    val sensorData = service.sensorData
    val sensorStatus = service.status

    init {
        statusMap[sensor] = AtrSensorStatus.OFFLINE
    }

    fun connect() {
        sensorStatusDisposable = service.status
                .subscribeOn(Schedulers.io())
                .subscribe {
                    statusMap[sensor] = it
                    statusStream.onNext(sensor to it)
                }
        service.connect()
    }

    fun disconnect() {
        service.disconnect()
        sensorDataSaver?.close()
        sensorDataDisposable?.dispose()
    }

    fun startMeasure() {
        if (isSave) {
            sensorDataSaver = SensorDataSaver(sensor.name)
        }
        sensorDataDisposable = service.sensorData
                .subscribeOn(Schedulers.io())
                .subscribe {
                    sensorDataSaver?.recordData(it)
                }
        service.startMeasure()
    }

    fun stopMeasure() {
        service.stopMeasure()
        sensorDataSaver?.close()
        sensorDataDisposable?.dispose()
    }
}