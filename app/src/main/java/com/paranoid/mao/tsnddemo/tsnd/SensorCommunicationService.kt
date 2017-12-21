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

    private val binder: IBinder = LocalBinder()
    private var sensorService: TSNDService? = null
    var isMeasuring = false

    inner class LocalBinder: Binder() {
        fun getService(): SensorCommunicationService = this@SensorCommunicationService
    }

    override fun onCreate() {
        sensorService = TSNDServiceImpl("00:07:80:76:87:6E")
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
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

    private fun showToast(text: String) = Handler(Looper.getMainLooper()).apply {
        post { Toast.makeText(this@SensorCommunicationService, text, Toast.LENGTH_SHORT).show() }
    }

    private fun showToast(textId: Int) = Handler(Looper.getMainLooper()).apply {
        post { Toast.makeText(this@SensorCommunicationService, textId, Toast.LENGTH_SHORT).show() }
    }
}
