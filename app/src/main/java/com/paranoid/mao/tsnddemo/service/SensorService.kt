package com.paranoid.mao.tsnddemo.service

import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.atrsensorservice.tsnd121.Tsnd121Service
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorResponse
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorService(private val sensor: Sensor,
                    private val isSave: Boolean = false) {

    private val service = Tsnd121Service(sensor.name, sensor.mac, 10)
    private var sensorDataSaver: SensorDataSaver? = null

    private var sensorDataDisposable: Disposable? = null

    private var sensorStatusDisposable: Disposable? = null

    fun connect() {
        sensorStatusDisposable = service.status
                .subscribeOn(Schedulers.io())
                .subscribe {
                    when(it) {
                        AtrSensorStatus.OFFLINE -> {
                            RxBus.publish(SensorResponse.Offline(sensor))
                        }
                        AtrSensorStatus.COMMAND -> {
                            RxBus.publish(SensorResponse.Command(sensor))
                        }
                        AtrSensorStatus.MEASURING -> {
                            RxBus.publish(SensorResponse.Measuring(sensor))
                        }
                    }
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
                    RxBus.publish(it)
                    if (isSave) sensorDataSaver?.recordData(it)
                }
        service.startMeasure()
    }

    fun stopMeasure() {
        service.stopMeasure()
        sensorDataSaver?.close()
        sensorDataDisposable?.dispose()
    }
}