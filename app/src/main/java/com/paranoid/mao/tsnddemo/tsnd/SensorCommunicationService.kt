package com.paranoid.mao.tsnddemo.tsnd

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.events.ConnectionEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.collections.HashMap

class SensorCommunicationService : Service() {

    private val binder: IBinder = LocalBinder()
//    private val sensorService = SensorService("Test", "00:07:80:76:87:6E")
//    private val sensorLeft = SensorService("left", "00:07:80:76:8F:35")
//    private val sensorRight = SensorService("right", "00:07:80:76:8E:B1")
//    private val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

    private val sensorMap = Collections.synchronizedMap<SensorInfo, SensorService>(HashMap())
    private val disposable = RxBus.listen(ConnectionEvent::class.java)
            .subscribe {
                when(it.command) {
                    ConnectionEvent.CONNECT -> connect(it.info)
                    ConnectionEvent.DISCONNECT -> disConnect(it.info)
                    ConnectionEvent.START -> startMeasure(it.info)
                    ConnectionEvent.STOP -> stopMeasure(it.info)
                    ConnectionEvent.REQUEST_STATUS -> sendStatus(it.info)
                }
            }

    var isNoConnection: Boolean = true
        private set
        get() = sensorMap.isEmpty()
    var numOfConnected: Int = 0
        private set
        get() = sensorMap.size

    inner class LocalBinder: Binder() {
        fun getService(): SensorCommunicationService = this@SensorCommunicationService
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        intent?.let { handleIntent(it) }
//        return super.onStartCommand(intent, flags, startId)
//    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private fun sendStatus(info: SensorInfo) {
        Log.v("info", "${info.toString()}, ${sensorMap[info].toString()}, ${sensorMap.size}")
        sensorMap[info]?.let { sensorManager ->
            RxBus.publish(ConnectionEvent(ConnectionEvent.STATUS, info, sensorManager.isConnected, sensorManager.isMeasuring))
        }
    }

    private fun connect(info: SensorInfo) {
        if (sensorMap[info] == null) sensorMap[info] = SensorService(info.name, info.mac)
        doAsync {
            val isConnect = sensorMap[info]?.connect()?: false
            if (isConnect) {
                sendStatus(info)
            } else {
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
                }
            }
        }
    }

    private fun stopMeasure(info: SensorInfo) {
        doAsync {
            sensorMap[info]?.let { sensorManager ->
                if (sensorManager.isConnected && sensorManager.isMeasuring) {
                    sensorManager.stop()
                }
            }
        }
    }

    private fun disConnectAll() {
        sensorMap.keys.toList().forEach {info ->
            disConnect(info)
        }
    }

    override fun onDestroy() {
        disConnectAll()
        disposable.dispose()
        super.onDestroy()
    }
}
