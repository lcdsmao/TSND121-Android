package com.paranoid.mao.tsnddemo.tsnd

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.events.Command
import com.paranoid.mao.tsnddemo.events.ConnectionEvent
import com.paranoid.mao.tsnddemo.events.MeasureEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.collections.HashMap

class SensorCommunicationService : Service() {

    private val binder: IBinder = LocalBinder()
//    private val sensorLeft = SensorService("left", "00:07:80:76:8F:35")
//    private val sensorRight = SensorService("right", "00:07:80:76:8E:B1")

    private val sensorMap = Collections.synchronizedMap<SensorInfo, SensorService>(HashMap())
    private val connectDisposable = RxBus.listen(ConnectionEvent::class.java)
            .subscribe {
                when(it.command) {
                    Command.CONNECT -> connect(it.info)
                    Command.DISCONNECT -> disConnect(it.info)
                    Command.REQUEST_STATUS -> sendStatus(it.info)
                    else -> return@subscribe
                }
            }
    private val measureDisposable = RxBus.listen(MeasureEvent::class.java)
            .subscribe {
                when(it.command) {
                    Command.MEASURE -> {
                        if (isAnyMeasuring) stopMeasureAll()
                        else startMeasureAll()
                    }
                    Command.REQUEST_STATUS -> {
                        sendAnyMeasuringStatus()
                    }
                    else -> return@subscribe
                }
            }

    var isNoConnected: Boolean = true
        private set
        get() = sensorMap.isEmpty()
    var isAnyMeasuring: Boolean = false
        private set
        get() = sensorMap.any { it.value.isMeasuring }

    inner class LocalBinder: Binder() {
        fun getService(): SensorCommunicationService = this@SensorCommunicationService
    }

    fun getSensorServiceFromId(id: Int): SensorService? = sensorMap.mapKeys { it.key.id } [id]

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private fun sendAnyMeasuringStatus() {
        RxBus.publish(MeasureEvent(Command.STATUS, isAnyMeasuring))
    }

    private fun sendStatus(info: SensorInfo) {
        Log.v("info", "${info.toString()}, ${sensorMap[info].toString()}, ${sensorMap.size}")
        sensorMap[info]?.let { sensorManager ->
            RxBus.publish(ConnectionEvent(Command.STATUS, info, sensorManager.isConnected, sensorManager.isMeasuring))
        }
    }

    private fun connect(info: SensorInfo) {
        if (sensorMap[info] == null) sensorMap[info] = SensorService(info.name, info.mac)
        doAsync {
            val isConnect = sensorMap[info]?.connect()?: false
            sendStatus(info)
            if (!isConnect) {
                sensorMap.remove(info)
            }
        }
    }

    private fun disConnect(info: SensorInfo) {
        doAsync {
            sensorMap[info]?.disconnect()
            sendStatus(info)
            sensorMap.remove(info)
        }
    }

    private fun startMeasure(info: SensorInfo) {
        doAsync {
            sensorMap[info]?.let { sensorManager ->
                if (sensorManager.isConnected && !sensorManager.isMeasuring) {
                    sensorManager.run()
                    sendStatus(info)
                }
            }
            sendAnyMeasuringStatus()
        }

    }

    private fun stopMeasure(info: SensorInfo) {
        doAsync {
            sensorMap[info]?.let { sensorManager ->
                if (sensorManager.isConnected && sensorManager.isMeasuring) {
                    sensorManager.stop()
                    sendStatus(info)
                }
            }
            sendAnyMeasuringStatus()
        }
    }

    private fun startMeasureAll() {
        sensorMap.keys.toList().forEach { info ->
            startMeasure(info)
        }
    }

    private fun stopMeasureAll() {
        sensorMap.keys.toList().forEach { info ->
            stopMeasure(info)
        }
    }

    private fun disConnectAll() {
        sensorMap.keys.toList().forEach {info ->
            disConnect(info)
        }
    }

    override fun onDestroy() {
        disConnectAll()
        connectDisposable.dispose()
        measureDisposable.dispose()
        super.onDestroy()
    }
}
