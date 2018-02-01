package com.paranoid.mao.tsnddemo.events

import com.paranoid.mao.tsnddemo.model.SensorInfo

/**
 * Created by Paranoid on 1/30/18.
 */
data class ConnectionEvent(val command: Command,
                           val info: SensorInfo,
                           val isConnect: Boolean = false,
                           val isMeasuring: Boolean = false)