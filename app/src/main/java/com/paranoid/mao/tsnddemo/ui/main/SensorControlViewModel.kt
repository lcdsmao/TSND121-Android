package com.paranoid.mao.tsnddemo.ui.main

import android.arch.lifecycle.ViewModel
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorType

class SensorControlViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val enabledSensorList = dataRepository.sensorStatusList
    var isSaveCsv: Boolean
        set(value) { dataRepository.isSaveCsv = value }
        get() = dataRepository.isSaveCsv

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