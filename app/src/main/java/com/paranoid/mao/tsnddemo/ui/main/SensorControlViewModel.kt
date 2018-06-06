package com.paranoid.mao.tsnddemo.ui.main

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorCommand
import com.paranoid.mao.tsnddemo.vo.SensorResponse
import java.util.*

class SensorControlViewModel(dataRepository: DataRepository) : ViewModel() {

    private val connectedSensorSet = Collections.synchronizedSet(mutableSetOf<Sensor>())
    private val measuringSensorSet = Collections.synchronizedSet(mutableSetOf<Sensor>())
    val isReadyMeasure: Boolean
        get() = connectedSensorSet.isNotEmpty() && measuringSensorSet.isEmpty()
    val isMeasuring: Boolean
        get() = measuringSensorSet.isNotEmpty()

    val enabledSensorList = dataRepository.sensorEnabledList
            .distinctUntilChanged()

    val sensorStatusUpdate = RxBus.listen(SensorResponse::class.java)
            .doOnNext {
                when(it) {
                    is SensorResponse.Command -> {
                        connectedSensorSet.add(it.sensor)
                        measuringSensorSet.remove(it.sensor)
                    }
                    is SensorResponse.Measuring -> {
                        connectedSensorSet.add(it.sensor)
                        measuringSensorSet.add(it.sensor)
                    }
                    is SensorResponse.Offline -> {
                        connectedSensorSet.remove(it.sensor)
                        measuringSensorSet.remove(it.sensor)
                    }
                }
            }

    fun connect(sensor: Sensor) {
        RxBus.publish(SensorCommand.Connect(sensor))
    }

    fun disConnect(sensor: Sensor) {
        RxBus.publish(SensorCommand.Disconnect(sensor))
    }

    fun startMeasureAll() = connectedSensorSet.forEach {
        startMeasure(it)
    }

    fun stopMeasureAll() = measuringSensorSet.forEach {
        stopMeasure(it)
    }

    private fun startMeasure(sensor: Sensor) {
        Log.v("MEASURE", sensor.toString())
        RxBus.publish(SensorCommand.StartMeasure(sensor))
    }

    private fun stopMeasure(sensor: Sensor) {
        RxBus.publish(SensorCommand.StopMeasure(sensor))
    }

    fun isSensorConnected(sensor: Sensor) = connectedSensorSet.contains(sensor)

    fun isSensorMeasuring(sensor: Sensor) = measuringSensorSet.contains(sensor)
}