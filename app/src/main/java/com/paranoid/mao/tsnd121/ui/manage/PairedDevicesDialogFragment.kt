package com.paranoid.mao.tsnd121.ui.manage

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.vo.Sensor
import org.jetbrains.anko.support.v4.alert
import org.koin.android.architecture.ext.sharedViewModel

class PairedDevicesDialogFragment: DialogFragment() {

    private val viewModel: ManageViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val devices = bluetoothAdapter.bondedDevices.toList()
        return alert {
            titleResource = R.string.from_paired_devices
            val deviceNames = devices
                    .map { it.name }
                    .filter { it.contains("TSND121") }
            items(deviceNames) { _, i ->
                val name = devices[i].name
                val mac = devices[i].address
                val sensor = Sensor(name, mac)
                viewModel.click(sensor)
            }
        }.build() as Dialog
    }
}