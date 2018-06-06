package com.paranoid.mao.tsnddemo.ui.manage

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.vo.Sensor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_sensor_list.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.koin.android.ext.android.inject

/**
 * A placeholder fragment containing a simple view.
 */
class SensorManageFragment : Fragment() {

    private val viewModel: ManageViewModel by inject()
    private val compositeDisposable = CompositeDisposable()
    private val listAdapter: SensorListAdapter by lazy { SensorListAdapter(viewModel) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sensor_list, container, false)
        view.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            }
            fabButton.apply {
                setImageResource(R.drawable.ic_add)
                setOnClickListener {
                    showEditDialog()
                }
            }
        }
        return view
    }

    private fun showEditDialog(old: Sensor? = null) {
        alert {
            var nameEdit: TextInputEditText? = null
            var macEdit: TextInputEditText? = null
            titleResource = if (old == null) R.string.add_sensor else R.string.modify_sensor
            customView {
                verticalLayout {
                    // Name input
                    textInputLayout {
                        nameEdit = textInputEditText {
                            setText(old?.name)
                            hintResource = R.string.add_sensor_name
                        }
                    }
                    // Address input
                    textInputLayout {
                        macEdit = textInputEditText {
                            setText((old?.mac))
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
                if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
                    toast(R.string.illegal)
                    return@positiveButton
                } else {
                    if (old == null) {
                        viewModel.insert(Sensor(name, mac))
                    } else {
                        val alter = old.copy(name = name, mac = mac)
                        viewModel.insert(alter)
                    }
                }
            }
            // Delete
            val negativeResource = if (old == null) R.string.cancel else R.string.delete
            negativeButton(negativeResource) {
                old?.let {
                    viewModel.delete(it)
                }
            }
        }.show()
    }

    override fun onStart() {
        super.onStart()
        viewModel.allSensorList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    listAdapter.sensorList = it
                    listAdapter.notifyDataSetChanged()
                }.addTo(compositeDisposable)
        viewModel.editEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showEditDialog(it)
                }.addTo(compositeDisposable)
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }
}
