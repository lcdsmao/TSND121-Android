package com.paranoid.mao.tsnddemo.tsnd

import jp.walkmate.tsndservice.Service.Impl.TSNDServiceImpl

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorManager(private val name: String, address: String) : TSNDServiceImpl(address) {

    var data: SensorData = SensorData()
        get() = SensorData(time - initTime, accX, accY, accZ, gyrX, gyrY, gyrZ, magX, magY, magZ)
        private set

    private var initTime = -1
    private var preRecordingCount = 0
    private var saver: SensorDataSaver? = null

    override fun run() {
        saver = SensorDataSaver(name)
        super.run()
    }

    override fun getSensorData(): Boolean {
        return if (super.getSensorData()) {
            if (preRecordingCount < 100) {
                preRecordingCount++
            }
            if (initTime < 0) {
                initTime = time
            }
            saver?.recordData(data)
            true
        } else {
            false
        }
    }
}