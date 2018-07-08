package com.paranoid.mao.tsnd121.data.sensor

import com.paranoid.mao.atrsensorservice.AccGyrData
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.atrsensorservice.MagData
import com.paranoid.mao.atrsensorservice.tsnd121.Tsnd121Service
import com.paranoid.mao.tsnd121.vo.AccGyrMagData
import com.paranoid.mao.tsnd121.vo.Sensor
import com.paranoid.mao.tsnd121.vo.SensorType
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorService(
        private val saveFileRootFolder: String,
        private val sensor: Sensor
) {

    private val samplingInterval = 10
    private val service = Tsnd121Service(sensor.name, sensor.mac, samplingInterval)
    private var sensorDataSaver: SensorDataSaver? = null
    private var sensorDataDisposable: Disposable? = null
    private var sensorStatusDisposable: Disposable? = null
    private var currentStatus: AtrSensorStatus = AtrSensorStatus.OFFLINE
    val sensorStatus = service.status
    val sensorData: Flowable<AccGyrMagData> = Flowable.combineLatest<AccGyrData, MagData, AccGyrMagData>(
            service.accGyrData,
            service.magData,
            BiFunction { accGyr: AccGyrData, mag: MagData ->
                AccGyrMagData(
                        time = accGyr.time,
                        accX = accGyr.accX,
                        accY = accGyr.accY,
                        accZ = accGyr.accZ,
                        gyrX = accGyr.gyrX,
                        gyrY = accGyr.gyrY,
                        gyrZ = accGyr.gyrZ,
                        magX = mag.magX,
                        magY = mag.magY,
                        magZ = mag.magZ
                )
            })
            .distinct(AccGyrMagData::time)
            .share()

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
            sensorDataSaver = SensorDataSaver(saveFileRootFolder, sensor.name)
        }
        sensorDataDisposable = sensorData
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
            SensorType.GYROSCOPE -> service.calibrateGyr()
            SensorType.MAGNETOMETER -> service.calibrateMag()
        }
    }
}