package com.paranoid.mao.tsnd121.ui.manage

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.vo.Sensor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_sensor_list.view.*
import org.koin.android.architecture.ext.sharedViewModel

/**
 * A placeholder fragment containing a simple view.
 */
class SensorManageFragment : Fragment() {

    private val viewModel: ManageViewModel by sharedViewModel()
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
                    showPairedDevices()
                }
            }
        }
        return view
    }

    private fun showEditDialog(old: Sensor) {
        val editDialogFragment = EditDialogFragment.newInstance(old)
        editDialogFragment.show(activity?.supportFragmentManager, "EDIT")
    }

    private fun showPairedDevices() {
        val fragment = PairedDevicesDialogFragment()
        fragment.show(activity?.supportFragmentManager, PairedDevicesDialogFragment::class.java.simpleName)
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
