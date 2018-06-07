package com.paranoid.mao.tsnddemo.ui.graph

import android.arch.lifecycle.ViewModel
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.vo.Sensor

class GraphViewModel(private val dataRepository: DataRepository)
    : ViewModel() {

    fun getSensorData(sensor: Sensor) = dataRepository.getSensorData(sensor)

}