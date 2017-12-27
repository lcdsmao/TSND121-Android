package com.paranoid.mao.tsnddemo.tsnd

import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.paranoid.mao.tsnddemo.R
import jp.walkmate.tsndservice.Listener.TSNDConnectionListener
import jp.walkmate.tsndservice.Thread.TSNDConnectionThread

class SensorCommunicationService : Service() {

    val ACTION_STOP_MEASURE = "Stop Measure"
    val ACTION_DISCONNECT = "Disconnect"

    private val binder: IBinder = LocalBinder()
    private val sensorService = SensorManager("Test", "00:07:80:76:87:6E")
    var isMeasuring = false
    var sensorData: SensorData = SensorData()
        get() = sensorService.data
        private set

    inner class LocalBinder: Binder() {
        fun getService(): SensorCommunicationService = this@SensorCommunicationService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_STOP_MEASURE -> stopMeasure()
            ACTION_DISCONNECT -> disconnectAll()
        }
    }

    fun connectAll() {
        val connectionThread = TSNDConnectionThread(sensorService)
        connectionThread.setConnectionListener(object : TSNDConnectionListener {
            override fun onConnected() {
                showToast(R.string.msg_connect_success)
            }

            override fun onFailedToConnect() {
                showToast(R.string.msg_connect_failed)
            }

        })
        connectionThread.start()
    }

    fun disconnectAll() {
        if (sensorService.isConnected) {
            sensorService.disconnect()
        }
    }

    fun startMeasure() {
        if (sensorService.isConnected && !isMeasuring) {
            isMeasuring = true
            sensorService.run()
        }
    }

    fun stopMeasure() {
        if (sensorService.isConnected && isMeasuring) {
            isMeasuring = false
            sensorService.stop()
        }
    }

    override fun onDestroy() {
        stopMeasure()
        disconnectAll()
        super.onDestroy()
    }

    private fun showToast(textId: Int) = Handler(Looper.getMainLooper()).apply {
        post { Toast.makeText(this@SensorCommunicationService, textId, Toast.LENGTH_SHORT).show() }
    }
}
