package com.paranoid.mao.tsnddemo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService.LocalBinder
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerViewAdapter
import com.jakewharton.rxbinding2.view.RxView
import com.paranoid.mao.tsnddemo.db.*
import com.paranoid.mao.tsnddemo.model.SensorInfo
import com.paranoid.mao.tsnddemo.tsnd.SensorCommunicationService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.sensor_list_item.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startService
import org.jetbrains.anko.stopService


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BT = 101
        private const val REQUEST_WRITE_PERMISSION = 102
    }

    private var sensorCommunicationService: SensorCommunicationService? = null
    private var bound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService<SensorCommunicationService>()
        requestBluetooth()
        requestPermission()

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SensorControlFragment())
                .commit()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, SensorCommunicationService::class.java), sensorConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(sensorConnection)
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) {
            stopService<SensorCommunicationService>()
        }
        super.onDestroy()
    }

    private fun requestPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_PERMISSION)
            return true
        }
        return false
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu?.findItem(R.id.manage)?.intent = Intent(ctx, ManageActivity::class.java)
        return super.onCreateOptionsMenu(menu)
    }
}
