package com.paranoid.mao.tsnd121.data

import android.content.SharedPreferences
import com.paranoid.mao.tsnd121.vo.AccGyrMagData
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.tsnd121.data.database.AppDatabase
import com.paranoid.mao.tsnd121.data.sensor.SensorService
import com.paranoid.mao.tsnd121.get
import com.paranoid.mao.tsnd121.put
import com.paranoid.mao.tsnd121.vo.Sensor
import com.paranoid.mao.tsnd121.vo.SensorType
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap

class DataRepository(
        private val database: AppDatabase,
        private val sharedPreferences: SharedPreferences) {

    private val enabledSensorMap: ConcurrentHashMap<Sensor, SensorService> = ConcurrentHashMap()
    val enabledSensorSet: Set<Sensor>
        get() = enabledSensorMap.keys

    val sensorList = database.sensorDao()
            .getAll()
            .subscribeOn(Schedulers.io())!!

    private val enabledSensorDisposable = database.sensorDao()
            .getEnabled()
            .subscribeOn(Schedulers.io())
            .subscribe {
                handleNewEnabledSensorList(it)
            }
    private var enabledSensorStatusDisposable = Disposables.empty()

    private val sensorStatusListProcessor: BehaviorProcessor<List<Pair<Sensor, AtrSensorStatus>>>
            = BehaviorProcessor.createDefault(emptyList())
    val sensorStatusList: Flowable<List<Pair<Sensor, AtrSensorStatus>>>
        get() = sensorStatusListProcessor

    var isSaveCsv: Boolean
        set(value) = sharedPreferences.put("save_csv", value)
        get() = sharedPreferences.get("save_csv", false)
    var gDirection: String
        set(value) = sharedPreferences.put("g_direction", value)
        get() = sharedPreferences.get("g_direction", "X")

    fun insert(vararg sensor: Sensor) = database.sensorDao().insert(*sensor)

    fun delete(sensor: Sensor) = database.sensorDao().delete(sensor)

    private fun handleNewEnabledSensorList(list: List<Sensor>) {
        enabledSensorMap.values.forEach {
            it.disconnect()
        }
        enabledSensorMap.clear()
        list.forEach {
            enabledSensorMap[it] = SensorService(it)
        }
        sensorStatusListProcessor.onNext(list.map {
            it to AtrSensorStatus.OFFLINE
        })

        enabledSensorStatusDisposable.dispose()
        enabledSensorStatusDisposable = Flowable.mergeDelayError(
                enabledSensorMap.entries
                        .map { entry ->
                            entry.value.sensorStatus.map {
                                entry.key to it
                            }
                        }
        ).subscribe {
            val oldList = sensorStatusListProcessor.value
            val newList = mutableListOf<Pair<Sensor, AtrSensorStatus>>()
            for (p in oldList) {
                newList += if (it.first == p.first) {
                    it
                } else {
                    p
                }
            }
            sensorStatusListProcessor.onNext(newList)
        }
    }

    fun connect(sensor: Sensor) {
        enabledSensorMap[sensor]?.connect()
    }

    fun disconnect(sensor: Sensor) {
        enabledSensorMap[sensor]?.disconnect()
    }

    fun startMeasure(sensor: Sensor) {
        enabledSensorMap[sensor]?.startMeasure(isSaveCsv)
    }

    fun stopMeasure(sensor: Sensor) {
        enabledSensorMap[sensor]?.stopMeasure()
    }

    fun getSensorData(sensor: Sensor): Flowable<AccGyrMagData>
            = enabledSensorMap[sensor]?.sensorData?: Flowable.empty()

    fun getSensorStatus(sensor: Sensor): Flowable<AtrSensorStatus>
            = enabledSensorMap[sensor]?.sensorStatus?: Flowable.empty()

    fun calibrate(sensor: Sensor, type: SensorType): Single<Boolean> {
        return enabledSensorMap[sensor]?.calibrate(type, gDirection)?: Single.just(false)
    }

    fun disconnectAll() {
        enabledSensorMap.values.forEach {
            it.disconnect()
        }
    }
}