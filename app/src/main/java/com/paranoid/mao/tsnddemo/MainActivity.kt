package com.paranoid.mao.tsnddemo

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService.LocalBinder
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService
import com.paranoid.mao.tsnddemo.tsnd.SensorData
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), View.OnClickListener{

    private val REQUEST_ENABLE_BT = 101
    private val UI_UPDATE_INTERVAL: Long = 200

    private var sensorCommunicationService: SensorCommunicationService? = null
    private var bound: Boolean = false

    private val handler = Handler()
    private val displaySensorDataRunnable: Runnable = object : Runnable {
        override fun run() {
            sensorCommunicationService?.getSensorData()?.displayText()
            handler.postDelayed(this, UI_UPDATE_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestBluetooth()
        connectButton.setOnClickListener(this)
        measureButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.connectButton -> {
                if (bound) {
//                    sensorCommunicationService?.let {
//                        if (it.isConnected) {
//                            it.disconnectAll()
//                            connectButton.setText(R.string.connect)
//                            measureButton.setText(R.string.start_measure)
//                        } else {
//                            it.connectAll()
//                            connectButton.setText(R.string.disconnect)
//                        }
//                    }
                    sensorCommunicationService?.connectAll()
                }
            }
            R.id.measureButton -> {
                if (bound) {
                    sensorCommunicationService?.let {
                        if (it.isMeasuring) {
                            it.stopMeasure()
                            measureButton.setText(R.string.start_measure)
                        } else {
                            it.startMeasure()
                            measureButton.setText(R.string.stop_measure)
                        }
                    }
                }
            }
        }
    }

    private fun requestBluetooth() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            finish()
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_ENABLE_BT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.msg_need_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(displaySensorDataRunnable, UI_UPDATE_INTERVAL)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(displaySensorDataRunnable)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SensorCommunicationService::class.java)
        startService(intent)
        bindService(intent, sensorConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(sensorConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            val intent = Intent(this, SensorCommunicationService::class.java)
            stopService(intent)
        }
    }

    private val sensorConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            sensorCommunicationService = binder.getService()
            bound = true
        }
    }

    private fun SensorData.displayText() {
        tvAccX.text = accX.toString()
        tvAccY.text = accY.toString()
        tvAccZ.text = accZ.toString()
        tvGyroX.text = gyroX.toString()
        tvGyroY.text = gyroY.toString()
        tvGyroZ.text = gyroZ.toString()
        tvMagX.text = magX.toString()
        tvMagY.text = magY.toString()
        tvMagZ.text = magZ.toString()
    }
}
