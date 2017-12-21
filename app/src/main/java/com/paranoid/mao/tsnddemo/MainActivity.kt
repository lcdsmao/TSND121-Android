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
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService.LocalBinder
import android.os.IBinder
import android.view.View
import android.widget.Toast
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService


class MainActivity : AppCompatActivity(), View.OnClickListener{

    private val REQUEST_ENABLE_BT = 101

    var sensorCommunicationService: SensorCommunicationService? = null
    var bound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestBluetooth()
    }

    override fun onClick(v: View) {
        when(v) {
            connectButton -> if (bound) sensorCommunicationService?.connectAll()
            measureButton -> {
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
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SensorCommunicationService::class.java)
        bindService(intent, sensorConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(sensorConnection)
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
}
