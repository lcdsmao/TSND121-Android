package com.paranoid.mao.tsnddemo.tsnd

import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.paranoid.mao.tsnddemo.R
import jp.walkmate.tsndservice.Listener.TSNDConnectionListener
import jp.walkmate.tsndservice.Service.Impl.TSNDServiceImpl
import jp.walkmate.tsndservice.Service.TSNDService
import jp.walkmate.tsndservice.Thread.TSNDConnectionThread


class SensorCommunicationService : Service() {

    val ACTION_STOP_MEASURE = "Stop Measure"
    val ACTION_DISCONNECT = "Disconnect"

    private val binder: IBinder = LocalBinder()
    private var sensorService: TSNDService? = null
    var isMeasuring = false
    var isConnected: Boolean = false
        get() = sensorService?.isConnected ?: false

    inner class LocalBinder: Binder() {
        fun getService(): SensorCommunicationService = this@SensorCommunicationService
    }

    override fun onCreate() {
        sensorService = TSNDServiceImpl("00:07:80:76:87:6E")
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
        sensorService?.let {
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
    }

    fun disconnectAll() {
        sensorService?.disconnect()
    }

    fun startMeasure() {
        sensorService?.let {
            if (it.isConnected) {
                isMeasuring = true
                it.run()
            }
        }
    }

    fun stopMeasure() {
        sensorService?.let {
            if (it.isConnected) {
                isMeasuring = false
                it.stop()
            }
        }
    }

    fun getSensorData(): SensorData = sensorService?.run {
            SensorData(0, accX, accY, accZ, gyrX, gyrY, gyrZ)
        } ?: SensorData()

    override fun onDestroy() {
        super.onDestroy()
        disconnectAll()
    }

    private fun showToast(text: String) = Handler(Looper.getMainLooper()).apply {
        post { Toast.makeText(this@SensorCommunicationService, text, Toast.LENGTH_SHORT).show() }
    }

    private fun showToast(textId: Int) = Handler(Looper.getMainLooper()).apply {
        post { Toast.makeText(this@SensorCommunicationService, textId, Toast.LENGTH_SHORT).show() }
    }
}
