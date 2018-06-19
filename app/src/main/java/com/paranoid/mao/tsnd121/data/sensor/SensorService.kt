package com.paranoid.mao.tsnd121.data.sensor

import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.atrsensorservice.tsnd121.Tsnd121Service
import com.paranoid.mao.tsnd121.vo.Sensor
import com.paranoid.mao.tsnd121.vo.SensorType
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorService(private val sensor: Sensor) {

    private val service = Tsnd121Service(sensor.name, sensor.mac, 10)
    private var sensorDataSaver: SensorDataSaver? = null
    private var sensorDataDisposable: Disposable? = null
    private var sensorStatusDisposable: Disposable? = null
    val sensorData = service.sensorData
    val sensorStatus = service.status
    private var currentStatus: AtrSensorStatus = AtrSensorStatus.OFFLINE

    fun connect() {
        sensorStatusDisposable = sensorStatus
                .subscribe {
                    currentStatus = it
                }
        service.connect()
    }

    fun disconnect() {
        service.disconnect()
        sensorDataSaver?.close()
        sensorStatusDisposable?.dispose()
        sensorDataDisposable?.dispose()
    }

    fun startMeasure(isSave: Boolean = false) {
        if (currentStatus != AtrSensorStatus.COMMAND) return
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

    fun calibrate(type: SensorType, gDirection: String = "Z"): Single<Boolean> {
        return when(type) {
            SensorType.ACCELEROMETER -> service.calibrateAcc(false, gDirection)
            SensorType.GYROSCOPE -> service.calibrateGyro()
            SensorType.MAGNETOMETER -> service.calibrateMag()
        }
    }
}