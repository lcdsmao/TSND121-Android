package com.paranoid.mao.tsnddemo.ui.manage

import android.arch.lifecycle.ViewModel
import android.bluetooth.BluetoothAdapter
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class ManageViewModel(
        private val dataRepository: DataRepository
) : ViewModel() {

    val allSensorList = dataRepository.sensorList
    private val clickSubject = PublishSubject.create<Sensor>()
    val editEvent = clickSubject.throttleFirst(1, TimeUnit.SECONDS)

    fun insert(sensor: Sensor): Int? {
        return if (BluetoothAdapter.checkBluetoothAddress(sensor.mac)) {
            launch {
                dataRepository.insert(sensor)
            }
            null
        } else {
            R.string.msg_illegal_mac_address
        }
    }

    fun delete(sensor: Sensor){
        if (sensor == Sensor.DUMMY) return
        launch {
            dataRepository.delete(sensor)
        }
    }

    fun click(sensor: Sensor) {
        clickSubject.onNext(sensor)
    }

    fun enable(sensor: Sensor, isEnable: Boolean) {
        insert(sensor.copy(enableStatus = isEnable))
    }
}