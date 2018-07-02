package com.paranoid.mao.tsnd121.ui.manage

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.vo.Sensor
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.koin.android.architecture.ext.sharedViewModel

class EditDialogFragment : DialogFragment() {

    private val viewModel: ManageViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val old: Sensor = arguments?.getParcelable("sensor")?: Sensor.DUMMY
        return alert {
            var nameEdit: TextInputEditText? = null
            var macEdit: TextInputEditText? = null
            titleResource = if (old == Sensor.DUMMY) R.string.add_sensor else R.string.modify_sensor
            customView {
                verticalLayout {
                    // Name input
                    textInputLayout {
                        nameEdit = textInputEditText {
                            setText(old.name)
                            hintResource = R.string.add_sensor_name
                        }
                    }
                    // Address input
                    textInputLayout {
                        macEdit = textInputEditText {
                            setText(old.mac)
                            hintResource = R.string.add_sensor_mac
                        }
                    }
                    lparams {
                        width = matchParent
                        padding = dip(16)
                    }
                }
            }
            // Save
            positiveButton(R.string.save) {
                val name = nameEdit?.text.toString()
                val mac = macEdit?.text.toString()
                val newOne = old.copy(name = name, mac = mac)
                viewModel.insert(newOne)?.let {
                    toast(it)
                }
            }
            // Delete
            negativeButton(R.string.delete) {
                viewModel.delete(old)
            }
            neutralPressed(R.string.from_paired_devices) {
                showPairedDevices()
            }
        }.build() as Dialog
    }

    private fun showPairedDevices() {
        val fragment = PairedDevicesDialogFragment()
        fragment.show(activity?.supportFragmentManager, PairedDevicesDialogFragment::class.java.simpleName)
    }

    companion object {
        fun newInstance(sensor: Sensor): EditDialogFragment {
            val fragment = EditDialogFragment()
            val arguments = Bundle().apply {
                putParcelable("sensor", sensor)
            }
            fragment.arguments = arguments
            return fragment
        }
    }

}