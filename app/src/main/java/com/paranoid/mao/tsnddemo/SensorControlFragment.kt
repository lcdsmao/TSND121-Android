package com.paranoid.mao.tsnddemo


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.paranoid.mao.tsnddemo.adapter.EnabledSensorListAdapter
import com.paranoid.mao.tsnddemo.db.DbManager
import com.paranoid.mao.tsnddemo.events.Command
import com.paranoid.mao.tsnddemo.events.MeasureEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class SensorControlFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()
    private val clickSubject = PublishSubject.create<SensorInfo>()
    private val listAdapter = EnabledSensorListAdapter(compositeDisposable, clickSubject)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        RxBus.publish(DbManager(ctx).loadEnabledSensorInfo())
        compositeDisposable += clickSubject
                .subscribe {
                    startActivity<GraphActivity>("id" to it.id)
                }
        return SensorListFragmentUI().createView(AnkoContext.create(ctx, this))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    inner class SensorListFragmentUI : AnkoComponent<SensorControlFragment> {
        override fun createView(ui: AnkoContext<SensorControlFragment>) = with(ui) {
            coordinatorLayout {
                // list
                recyclerView {
                    layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
                    adapter = listAdapter
                    val decoration = DividerItemDecoration(ctx, RecyclerView.VERTICAL).apply {
                        setDrawable(ctx.getDrawable(R.drawable.divider))
                    }
                    addItemDecoration(decoration)
                }.lparams(matchParent, matchParent)
                // float
                floatingActionButton {
                    size = FloatingActionButton.SIZE_NORMAL
                    setImageResource(R.drawable.ic_measure)
                    // Set click
                    compositeDisposable += RxView.clicks(this)
                            .throttleFirst(500, TimeUnit.MILLISECONDS)
                            .subscribe { RxBus.publish(MeasureEvent(Command.MEASURE)) }
                    // Listen to change icon
                    compositeDisposable += RxBus.listen(MeasureEvent::class.java)
                            .filter { it.command == Command.STATUS }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val resIcon = if (it.isAnyMeasuring) R.drawable.ic_measure_stop else R.drawable.ic_measure
                                setImageResource(resIcon)
                            }
                    RxBus.publish(MeasureEvent(Command.REQUEST_STATUS))
                }.lparams {
                    gravity = Gravity.END or Gravity.BOTTOM
                    margin = dip(16)
                }
            }
        }
    }
}
