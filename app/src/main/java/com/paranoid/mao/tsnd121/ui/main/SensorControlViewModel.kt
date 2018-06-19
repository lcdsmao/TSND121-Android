package com.paranoid.mao.tsnd121.ui.main

import android.arch.lifecycle.ViewModel
import com.paranoid.mao.tsnd121.data.DataRepository
import com.paranoid.mao.tsnd121.vo.Sensor
import com.paranoid.mao.tsnd121.vo.SensorType

class SensorControlViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val enabledSensorList = dataRepository.sensorStatusList
    var isSaveCsv: Boolean
        set(value) { dataRepository.isSaveCsv = value }
        get() = dataRepository.isSaveCsv
    var gDirection: String
        set(value) { dataRepository.gDirection = value }
        get() = dataRepository.gDirection

    fun connect(sensor: Sensor) {
        dataRepository.connect(sensor)
    }

    fun disconnect(sensor: Sensor) {
        dataRepository.disconnect(sensor)
    }

    fun startMeasureAll() {
        for (sensor in dataRepository.enabledSensorSet) {
            dataRepository.startMeasure(sensor)
        }
    }

    fun stopMeasureAll() {
        for (sensor in dataRepository.enabledSensorSet) {
            dataRepository.stopMeasure(sensor)
        }
    }

    fun disconnectAll() {
        dataRepository.disconnectAll()
    }

    fun calibrate(sensor: Sensor, type: SensorType)
            = dataRepository.calibrate(sensor, type)
}