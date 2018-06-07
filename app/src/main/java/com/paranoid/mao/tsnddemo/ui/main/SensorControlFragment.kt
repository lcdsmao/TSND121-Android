package com.paranoid.mao.tsnddemo.ui.main


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.ui.graph.GraphActivity
import com.paranoid.mao.tsnddemo.vo.Sensor
import com.paranoid.mao.tsnddemo.vo.SensorResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_sensor_list.*
import kotlinx.android.synthetic.main.fragment_sensor_list.view.*
import org.jetbrains.anko.support.v4.startActivity
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
    private val listAdapter: SensorListAdapter by lazy {
        SensorListAdapter(viewModel, onItemClick)
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
