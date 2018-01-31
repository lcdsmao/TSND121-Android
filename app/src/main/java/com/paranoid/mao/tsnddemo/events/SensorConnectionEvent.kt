package com.paranoid.mao.tsnddemo.events

import com.paranoid.mao.tsnddemo.model.SensorInfo

/**
 * Created by Paranoid on 1/30/18.
 */
data class SensorConnectionEvent(val command: String,
                                 val info: SensorInfo,
                                 val isConnect: Boolean = false,
                                 val isMeasuring: Boolean = false) {
    companion object {
        const val REQUEST_STATUS = "request"
        const val STATUS = "send"
        const val CONNECT = "connect"
        const val START = "start"
        const val STOP = "stop"
        const val DISCONNECT = "disconnect"
    }
}