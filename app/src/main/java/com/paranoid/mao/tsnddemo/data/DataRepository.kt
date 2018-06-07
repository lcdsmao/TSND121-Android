package com.paranoid.mao.tsnddemo.data

import android.content.SharedPreferences
import com.paranoid.mao.atrsensorservice.AccGyroMagData
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.tsnddemo.data.database.AppDatabase
import com.paranoid.mao.tsnddemo.data.sensor.SensorService
import com.paranoid.mao.tsnddemo.get
import com.paranoid.mao.tsnddemo.put
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap

class DataRepository(
        private val database: AppDatabase,
        private val sharedPreferences: SharedPreferences) {

    private val enabledSensorMap: ConcurrentHashMap<Sensor, SensorService> = ConcurrentHashMap()
    val enabledSensorStatusMap: ConcurrentHashMap<Sensor, AtrSensorStatus> = ConcurrentHashMap()
    val enabledSensorStatusStream: PublishProcessor<Pair<Sensor, AtrSensorStatus>> = PublishProcessor.create()
    val enabledSensorSet: Set<Sensor>
        get() = enabledSensorMap.keys

    val sensorList = database.sensorDao()
            .getAll()
            .subscribeOn(Schedulers.io())

    val sensorEnabledList = database.sensorDao()
            .getEnabled()
            .subscribeOn(Schedulers.io())
            .doOnNext { list ->
                val disabledSensors = enabledSensorMap.keys.subtract(list)
                val isSave = isSaveCsv
                for (sensor in disabledSensors) {
                    enabledSensorMap[sensor]?.disconnect()
                    enabledSensorMap.remove(sensor)
                }
                for (sensor in list) {
                    val service = enabledSensorMap[sensor]
                    if (service == null) {
                        enabledSensorMap[sensor] = SensorService(sensor, isSave,
                                enabledSensorStatusMap, enabledSensorStatusStream)
                    } else {
                        service.isSave = isSave
                    }
                }
            }

    var isSaveCsv: Boolean
        set(value) = sharedPreferences.put("save_csv", value)
        get() = sharedPreferences.get("save_csv", false)

    fun insert(vararg sensor: Sensor) = database.sensorDao().insert(*sensor)

    fun delete(sensor: Sensor) = database.sensorDao().delete(sensor)

    fun connect(sensor: Sensor) {
        enabledSensorMap[sensor]?.connect()
    }

    fun disconnect(sensor: Sensor) {
        enabledSensorMap[sensor]?.disconnect()
    }

    fun startMeasure(sensor: Sensor) {
        enabledSensorMap[sensor]?.startMeasure()
    }

    fun stopMeasure(sensor: Sensor) {
        enabledSensorMap[sensor]?.stopMeasure()
    }

    fun getSensorData(sensor: Sensor): Flowable<AccGyroMagData>
            = enabledSensorMap[sensor]?.sensorData?: Flowable.empty()

    fun getSensorStatus(sensor: Sensor): Flowable<AtrSensorStatus>
            = enabledSensorMap[sensor]?.sensorStatus?: Flowable.empty()
}