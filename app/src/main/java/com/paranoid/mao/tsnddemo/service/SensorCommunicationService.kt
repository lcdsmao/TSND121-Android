package com.paranoid.mao.tsnddemo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.paranoid.mao.tsnddemo.RxBus
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorCommand
import com.paranoid.mao.tsnddemo.vo.SensorResponse
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ConcurrentHashMap

class SensorCommunicationService : Service() {

//    private val binder: IBinder = LocalBinder()
//    private val sensorLeft = SensorService("left", "00:07:80:76:8F:35")
//    private val sensorRight = SensorService("right", "00:07:80:76:8E:B1")
//    private var isSaveCsv by DelegatedPreferences(this, PrefKeys.IS_SAVE_CSV, false)

    private val sensorMap = ConcurrentHashMap<Sensor, SensorService>()
    private val compositeDisposable = CompositeDisposable()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        listenCommand().addTo(compositeDisposable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    private fun listenCommand() = RxBus.listen(SensorCommand::class.java)
            .distinctUntilChanged()
            .subscribe {
                Log.v("SERVICE", it.sensor.toString())
                when(it) {
                    is SensorCommand.Connect -> connect(it.sensor)
                    is SensorCommand.Disconnect -> disconnect(it.sensor)
                    is SensorCommand.StartMeasure -> startMeasure(it.sensor)
                    is SensorCommand.StopMeasure -> stopMeasure(it.sensor)
                }
            }

    private fun connect(sensor: Sensor) {
        if (sensorMap[sensor] == null) {
            sensorMap[sensor] = SensorService(sensor.name, sensor.mac)
        }
        sensorMap[sensor].action(
                SensorResponse.Connected(sensor),
                SensorResponse.Failed(sensor)
        ) {
            connect()
        }
    }
    
    private fun disconnect(sensor: Sensor) {
        sensorMap[sensor].action(
                SensorResponse.Disconnected(sensor),
                SensorResponse.Failed(sensor)
        ) {
            disconnect()
        }
        sensorMap.remove(sensor)
    }
    
    private fun startMeasure(sensor: Sensor) {
        Log.v("SERVICE", "START")
        sensorMap[sensor].action(
                SensorResponse.MeasureStarted(sensor),
                SensorResponse.Failed(sensor)
        ) {
            start(false)
            true
        }
    }
    
    private fun stopMeasure(sensor: Sensor) {
        Log.v("SERVICE", "STOP")
        sensorMap[sensor].action(
                SensorResponse.MeasureStopped(sensor),
                SensorResponse.Failed(sensor)
        ) {
            stop()
            true
        }
    }

    private fun SensorService?.action(success: SensorResponse,
                                      fail: SensorResponse,
                                      command: SensorService.() -> Boolean) {
        if (this == null) {
            RxBus.publish(fail)
        } else {
            launch(CommonPool) {
                val isSuccess: Boolean = async {
                    command()
                }.await()
                if (isSuccess) RxBus.publish(success)
                else RxBus.publish(fail)
            }
        }
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
        Log.v("SERVICE", "DESTROY")
        disConnectAll()
        compositeDisposable.clear()
        super.onDestroy()
    }
}
