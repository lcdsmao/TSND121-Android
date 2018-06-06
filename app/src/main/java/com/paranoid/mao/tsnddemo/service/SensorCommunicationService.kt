package com.paranoid.mao.tsnddemo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorCommand
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

class SensorCommunicationService : Service() {

    private val sensorMap = ConcurrentHashMap<Sensor, SensorService>()
    private lateinit var disposableListener: Disposable

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        disposableListener = listenCommand()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    private fun listenCommand() = RxBus.listen(SensorCommand::class.java)
            .distinctUntilChanged()
            .subscribe {
                Log.v("Command", it.toString())
                when(it) {
                    is SensorCommand.Connect -> connect(it.sensor)
                    is SensorCommand.Disconnect -> disconnect(it.sensor)
                    is SensorCommand.StartMeasure -> startMeasure(it.sensor)
                    is SensorCommand.StopMeasure -> stopMeasure(it.sensor)
                }
            }

    private fun connect(sensor: Sensor) {
        if (sensorMap[sensor] == null) {
            sensorMap[sensor] = SensorService(sensor)
        }
        sensorMap[sensor]?.connect()
    }
    
    private fun disconnect(sensor: Sensor) {
        val service = sensorMap[sensor]?: return
        service.disconnect()
    }
    
    private fun startMeasure(sensor: Sensor) {
        val service = sensorMap[sensor]?: return
        service.startMeasure()
    }
    
    private fun stopMeasure(sensor: Sensor) {
        val service = sensorMap[sensor]?: return
        service.stopMeasure()
    }

    private fun startMeasureAll() {
        sensorMap.keys.toList().forEach { sensor ->
            startMeasure(sensor)
        }
    }

    private fun stopMeasureAll() {
        sensorMap.keys.toList().forEach { sensor ->
            stopMeasure(sensor)
        }
    }
    
    private fun connectAll() {
        sensorMap.keys().toList().forEach { sensor ->
            startMeasure(sensor)
        }
    }

    private fun disConnectAll() {
        sensorMap.keys.toList().forEach { sensor ->
            disconnect(sensor)
        }
    }

    override fun onDestroy() {
        disConnectAll()
        disposableListener.dispose()
        super.onDestroy()
    }
}
