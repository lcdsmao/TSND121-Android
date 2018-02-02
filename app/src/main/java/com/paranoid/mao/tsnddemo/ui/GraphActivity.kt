package com.paranoid.mao.tsnddemo.ui

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.model.SensorData
import com.paranoid.mao.tsnddemo.service.SensorCommunicationService
import com.paranoid.mao.tsnddemo.service.SensorService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.ctx
import java.util.concurrent.TimeUnit

class GraphActivity : AppCompatActivity() {

    private var sensorCommunicationService: SensorCommunicationService? = null

    private var accFragment: RealtimeGraphFragment? = null
    private var gyroFragment: RealtimeGraphFragment? = null
    private var magFragment: RealtimeGraphFragment? = null

    private var id: Int = 0
    private var sensorService: SensorService? = null
        get() {
            if (field == null) {
                field = sensorCommunicationService?.getSensorServiceFromId(id)
            }
            return field
        }

    private val flowable = Flowable.interval(100, TimeUnit.MILLISECONDS)
            .map { sensorService?.data ?: SensorData() }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        intent?.let {
            id = it.getIntExtra("id", 0)
        }

        // 8G
        accFragment = RealtimeGraphFragment.newInstance("Accelerator", -6.0, 6.0)
        // 1000dps
        gyroFragment = RealtimeGraphFragment.newInstance("Gyroscope", -800.0, 800.0)
        // 1200uT
        magFragment = RealtimeGraphFragment.newInstance("Magnetic", -200.0, 200.0)

        supportFragmentManager.beginTransaction()
                .replace(R.id.acc_container, accFragment)
                .replace(R.id.gyro_container, gyroFragment)
                .replace(R.id.mag_container, magFragment)
                .commit()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(ctx, SensorCommunicationService::class.java)
        bindService(intent, sensorConnection, Service.BIND_AUTO_CREATE)
        disposable = flowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it.apply {
                        // ms -> s
                        val t = time / 1000.0
                        accFragment?.addData(t, accX / 10000.0, accY / 10000.0, accZ / 10000.0)
                        gyroFragment?.addData(t, gyroX / 100.0, gyroY / 100.0, gyroZ / 100.0)
                        magFragment?.addData(t, magX / 10.0, magY / 10.0, magZ / 10.0)
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
        unbindService(sensorConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private val sensorConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            sensorCommunicationService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SensorCommunicationService.LocalBinder
            sensorCommunicationService = binder.getService()
        }
    }
}
