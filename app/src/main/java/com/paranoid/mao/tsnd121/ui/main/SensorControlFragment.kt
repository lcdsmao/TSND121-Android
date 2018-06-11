package com.paranoid.mao.tsnd121.ui.main


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.ui.graph.GraphActivity
import com.paranoid.mao.tsnd121.vo.Sensor
import com.paranoid.mao.tsnd121.vo.SensorType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_sensor_list.*
import kotlinx.android.synthetic.main.fragment_sensor_list.view.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
 */
class SensorControlFragment : Fragment() {

    private val viewModel: SensorControlViewModel by inject()
    private val compositeDisposable = CompositeDisposable()
    private val onItemClick: (Sensor) -> Unit = {
        startActivity<GraphActivity>(
                "sensor" to it
        )
    }
    private val onItemLongClick: (Sensor) -> Unit = { sensor ->
        createCalibrationDialog(sensor)
    }
    private val listAdapter: SensorListAdapter by lazy {
        SensorListAdapter(viewModel, onItemClick, onItemLongClick)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sensor_list, container, false)

        return view.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            }
            fabButton.apply {
                setImageResource(R.drawable.selector_measure)
                setOnClickListener {
                    if (!it.isActivated) {
                        viewModel.startMeasureAll()
                    } else {
                        viewModel.stopMeasureAll()
                    }
                }
            }
        }
    }

    private fun createCalibrationDialog(sensor: Sensor) {
        val itemList = resources.getStringArray(R.array.calibration).asList()
        alert {
            titleResource = R.string.calibration
            items(itemList) { _, item, _ ->
                val type = when (item) {
                    getString(R.string.acc) -> {
                        SensorType.ACCELEROMETER
                    }
                    getString(R.string.gyro) -> {
                        SensorType.GYROSCOPE
                    }
                    getString(R.string.mag) -> {
                        SensorType.MAGNETOMETER
                    }
                    else -> {
                        return@items
                    }
                }
                viewModel.calibrate(sensor, type)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            toast(R.string.msg_start_calibration)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it) toast(R.string.msg_success)
                            else toast(R.string.msg_failed)
                        }, {
                            toast(R.string.msg_failed)
                        })
                        .addTo(compositeDisposable)
            }
        }.show()
    }

    override fun onStart() {
        viewModel.enabledSensorList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    listAdapter.sensorList = list
                    listAdapter.notifyDataSetChanged()
                    fabButton.isActivated = list.any { it.second == AtrSensorStatus.MEASURING }
                }
                .addTo(compositeDisposable)
        super.onStart()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    companion object {
        fun newInstance(): SensorControlFragment = SensorControlFragment()
    }

}
