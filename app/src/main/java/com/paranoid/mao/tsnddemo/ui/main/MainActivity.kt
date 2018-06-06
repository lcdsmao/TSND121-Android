package com.paranoid.mao.tsnddemo.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.ui.manage.ManageActivity
import com.paranoid.mao.tsnddemo.replaceFragmentInActivity
import com.paranoid.mao.tsnddemo.service.SensorCommunicationService
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BT = 101
        private const val REQUEST_WRITE_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService<SensorCommunicationService>()
        requestBluetooth()
        requestPermission()

        replaceFragmentInActivity(SensorControlFragment.newInstance(), R.id.fragment_container)
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
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.msg_need_bluetooth, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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

    override fun onDestroy() {
        Log.v("MAIN", "OnDestroy")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.manage_menu -> {
                startActivity<ManageActivity>()
                true
            }
            R.id.save_csv -> {
                item.isChecked = !item.isChecked

                true
            }
            else -> false
        }
    }
}
