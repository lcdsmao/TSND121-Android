package com.paranoid.mao.tsnddemo.tsnd

import com.paranoid.mao.tsnddemo.model.SensorData
import jp.walkmate.tsndservice.Service.Impl.TSNDServiceImpl

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorService(val name: String, address: String) : TSNDServiceImpl(address) {

    var data: SensorData = SensorData()
        get() = SensorData(time, accX, accY, accZ, gyrX, gyrY, gyrZ, magX, magY, magZ)
        private set

    private var saver: SensorDataSaver? = null
    var isMeasuring = false

    override fun run() {
        saver = SensorDataSaver(name)
        isMeasuring = true
        super.run()
    }

    override fun stop() {
        super.stop()
        isMeasuring = false
        saver = null
    }

    override fun getSensorData(): Boolean {
        return if (super.getSensorData()) {
            saver?.recordData(data)
            true
        } else {
            false
        }
    }
}