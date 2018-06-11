package com.paranoid.mao.tsnd121.ui.graph

import android.arch.lifecycle.ViewModel
import com.paranoid.mao.tsnd121.data.DataRepository
import com.paranoid.mao.tsnd121.vo.Sensor

class GraphViewModel(private val dataRepository: DataRepository)
    : ViewModel() {

    fun getSensorData(sensor: Sensor) = dataRepository.getSensorData(sensor)

}