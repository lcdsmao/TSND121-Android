package com.paranoid.mao.tsnddemo

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.tsnddemo.adapter.AllSensorListAdapter
import com.paranoid.mao.tsnddemo.db.DbManager
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast

/**
 * A placeholder fragment containing a simple view.
 */
class SensorManageFragment : Fragment() {

    private var adapter: AllSensorListAdapter? = null
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = AllSensorListAdapter(DbManager(ctx).loadSensorInfo().toMutableList())
                .apply {
                    val disposable = subject
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { position ->
                                showEditDialog(position, sensorInfoList[position])
                            }
                    compositeDisposable.add(disposable)
                }
        return SensorEditFragmentUI().createView(AnkoContext.create(ctx, this))
    }

    private fun showEditDialog(position: Int = 0, oldInfo: SensorInfo? = null) {
        alert {
            var nameEdit: TextInputEditText? = null
            var macEdit: TextInputEditText? = null
            titleResource = if (oldInfo == null) R.string.add_sensor else R.string.modify_sensor
            customView {
                verticalLayout {
                    // Name input
                    textInputLayout {
                        nameEdit = textInputEditText {
                            setText(oldInfo?.name)
                             hintResource = R.string.add_sensor_name
                        }
                    }
                    // Address input
                    textInputLayout {
                        macEdit = textInputEditText {
                            setText((oldInfo?.mac))
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
                if (!isIllegalMAC(mac)) {
                    toast(R.string.illegal)
                    return@positiveButton
                }
                if (oldInfo == null) {
                    val info = SensorInfo(0, name, mac, false)
                    adapter?.apply {
                        sensorInfoList.add(info)
                        notifyItemInserted(itemCount - 1)
                    }
                } else {
                    val info = SensorInfo(oldInfo.id, name, mac, false)
                    adapter?.apply {
                        sensorInfoList[position] = info
                        notifyItemChanged(position)
                    }
                }
            }
            // Delete
            negativeButton(R.string.delete) {
                oldInfo?.let {
                    adapter?.apply {
                        sensorInfoList.removeAt(position)
                        notifyItemChanged(position)
                    }
                }
            }
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        doAsync {
            DbManager(ctx).apply {
                adapter?.sensorInfoList?.toList()?.let {
                    deleteAll()
                    insertList(it)
                    RxBus.publish(loadEnabledSensorInfo())
                }
            }
        }
    }

    private fun isIllegalMAC(mac: String): Boolean =
            mac.matches(Regex("^([0-9A-F]{2}:){5}([0-9A-F]{2})$"))

    inner class SensorEditFragmentUI : AnkoComponent<SensorManageFragment> {
        override fun createView(ui: AnkoContext<SensorManageFragment>) = with(ui) {
            coordinatorLayout {
                recyclerView {
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = this@SensorManageFragment.adapter
                    val itemDecoration = DividerItemDecoration(ctx, LinearLayoutManager.VERTICAL).apply {
                        setDrawable(ctx.getDrawable(R.drawable.divider))
                    }
                    addItemDecoration(itemDecoration)
                }.lparams(matchParent, matchParent)
                floatingActionButton {
                    imageResource = R.drawable.ic_add
                    size = FloatingActionButton.SIZE_NORMAL
                    setOnClickListener { showEditDialog() }
                }.lparams {
                    gravity = Gravity.END or Gravity.BOTTOM
                    margin = dip(16)
                }
            }
        }
    }
}
