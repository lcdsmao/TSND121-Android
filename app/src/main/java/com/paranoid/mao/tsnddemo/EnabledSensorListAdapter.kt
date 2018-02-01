package com.paranoid.mao.tsnddemo

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.jakewharton.rxbinding2.view.RxView
import com.paranoid.mao.tsnddemo.events.Command
import com.paranoid.mao.tsnddemo.events.ConnectionEvent
import com.paranoid.mao.tsnddemo.events.MeasureEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.sensor_list_item.view.*
import org.jetbrains.anko.find
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Paranoid on 1/25/18.
 */
class EnabledSensorListAdapter(private val compositeDisposable: CompositeDisposable) : RecyclerView.Adapter<EnabledSensorListAdapter.ViewHolder>() {

    var sensorInfoList: List<SensorInfo> = Collections.synchronizedList(ArrayList())
        set(value) {
            field = Collections.synchronizedList(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val switch = view.find<Switch>(R.id.sensorSwitch)
        val holder = ViewHolder(view)

        // Set click listener
        val clickDisposable = RxView.clicks(switch)
                .subscribeOn(AndroidSchedulers.mainThread())
//                .throttleFirst(2, TimeUnit.SECONDS)
                .map { switch.isChecked }
                .doOnNext { switch.isEnabled = false }
                .observeOn(Schedulers.newThread())
                .subscribe {
                    val command = if (it) Command.CONNECT else Command.DISCONNECT
                    RxBus.publish(ConnectionEvent(command, sensorInfoList[holder.adapterPosition]))
                }
        compositeDisposable.add(clickDisposable)

        // Set state listener
        val connectDisposable = RxBus.listen(ConnectionEvent::class.java)
                .filter { it.command == Command.STATUS && it.info == sensorInfoList[holder.adapterPosition] }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    switch.isChecked = it.isConnect
                    switch.isEnabled = true
                    view.isActivated = it.isMeasuring
                }
        compositeDisposable.add(connectDisposable)

        // Set measure listener
        val measureDisposable = RxBus.listen(MeasureEvent::class.java)
                .filter { it.command == Command.STATUS }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    switch.isEnabled = !it.isAnyMeasuring
                }
        compositeDisposable.add(measureDisposable)
        return holder
    }

    override fun getItemCount(): Int = sensorInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Request status
        Log.v("Bind", "$position")
        RxBus.publish(ConnectionEvent(Command.REQUEST_STATUS, sensorInfoList[position]))
        RxBus.publish(MeasureEvent(Command.REQUEST_STATUS))
        holder.bind(sensorInfoList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(info: SensorInfo) = with(itemView) {
            sensorNameTextView.text = info.name
            sensorMACTextView.text = info.mac
            sensorCheck.visibility = View.GONE
            sensorSwitch.visibility = View.VISIBLE
        }
    }
}