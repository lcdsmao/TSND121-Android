package com.paranoid.mao.tsnd121.ui.graph

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.vo.Sensor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class GraphActivity : AppCompatActivity() {

    private var accFragment: RealtimeGraphFragment? = null
    private var gyroFragment: RealtimeGraphFragment? = null
    private var magFragment: RealtimeGraphFragment? = null

    private lateinit var disposable: Disposable
    private lateinit var sensor: Sensor
    private val viewModel: GraphViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        sensor = intent?.getParcelableExtra("sensor")?: Sensor.DUMMY

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
        disposable = viewModel.getSensorData(sensor)
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    // ms -> s
                    val t = it.time / 1000.0
                    accFragment?.addData(t, it.accX / 10000.0, it.accY / 10000.0, it.accZ / 10000.0)
                    gyroFragment?.addData(t, it.gyroX / 100.0, it.gyroY / 100.0, it.gyroZ / 100.0)
                    magFragment?.addData(t, it.magX / 10.0, it.magY / 10.0, it.magZ / 10.0)
                }
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

}
