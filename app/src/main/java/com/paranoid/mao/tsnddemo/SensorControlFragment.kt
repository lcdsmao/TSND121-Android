package com.paranoid.mao.tsnddemo


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paranoid.mao.tsnddemo.db.DbManager
import com.paranoid.mao.tsnddemo.events.SensorConnectionEvent
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class SensorControlFragment : Fragment() {

    private var compositeDisposable = CompositeDisposable()
    private val listAdapter = EnabledSensorListAdapter(compositeDisposable)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        Log.v("Fragment", "On create")
        listAdapter.sensorInfoList = DbManager(ctx).loadEnabledSensorInfo()
        return SensorListFragmentUI().createView(AnkoContext.create(ctx, this))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.v("Fragment", "On destroy")
        compositeDisposable.dispose()
    }

    inner class SensorListFragmentUI : AnkoComponent<SensorControlFragment> {
        override fun createView(ui: AnkoContext<SensorControlFragment>) = with(ui) {
            coordinatorLayout {
                recyclerView {
                    layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
                    adapter = listAdapter
                    val decoration = DividerItemDecoration(ctx, RecyclerView.VERTICAL).apply {
                        setDrawable(ctx.getDrawable(R.drawable.divider))
                    }
                    addItemDecoration(decoration)
                }.lparams(matchParent, matchParent)

                floatingActionButton {
                    size = FloatingActionButton.SIZE_NORMAL
                    setImageResource(R.drawable.ic_play_arrow)
                    setOnClickListener {
                        listAdapter.sensorInfoList.forEach {
                            val event = SensorConnectionEvent(SensorConnectionEvent.CONNECT, it)
                            RxBus.publish(event)
                        }
                    }
                }.lparams {
                    gravity = Gravity.END or Gravity.BOTTOM
                    margin = dip(16)
                }
            }
        }
    }
}
