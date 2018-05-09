package com.paranoid.mao.tsnddemo.ui.main


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.ViewModelFactory
import com.paranoid.mao.tsnddemo.vo.SensorResponse
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_sensor_list.*
import kotlinx.android.synthetic.main.fragment_sensor_list.view.*
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SensorControlFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    private val compositeDisposable = CompositeDisposable()

    private val listAdapter by lazy { SensorListAdapter(viewModel) }

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
                isActivated = viewModel.isMeasuring
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
                .subscribe { it ->
                    listAdapter.sensorList = it
                    listAdapter.notifyDataSetChanged()
                }.addTo(compositeDisposable)

        viewModel.sensorStatusUpdate
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when(it) {
                        is SensorResponse.Failed -> toast(R.string.msg_failed)
                        is SensorResponse.MeasureStarted -> fabButton.isActivated = true
                        is SensorResponse.MeasureStopped -> fabButton.isActivated = false
                    }
                    listAdapter.notifyDataSetChanged()
                }.addTo(compositeDisposable)
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
