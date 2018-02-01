package com.paranoid.mao.tsnddemo

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.view.activated
import com.jakewharton.rxbinding2.view.enabled
import com.jakewharton.rxbinding2.widget.checked
import com.paranoid.mao.tsnddemo.events.ConnectionEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.sensor_list_item.view.*
import org.jetbrains.anko.find
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by Paranoid on 1/25/18.
 */
class EnabledSensorListAdapter(private val disposable: CompositeDisposable) : RecyclerView.Adapter<EnabledSensorListAdapter.ViewHolder>() {

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
                    val command = if (it) ConnectionEvent.CONNECT else ConnectionEvent.DISCONNECT
                    RxBus.publish(ConnectionEvent(command, sensorInfoList[holder.adapterPosition]))
                }
        disposable.add(clickDisposable)

        // Set state listener
        val busDisposable = RxBus.listen(ConnectionEvent::class.java)
                .filter { it.command == ConnectionEvent.STATUS && it.info == sensorInfoList[holder.adapterPosition] }
                .map { it.isConnect }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    switch.isChecked = it
                    switch.isEnabled = true
                }
        disposable.add(busDisposable)
        return holder
    }

    override fun getItemCount(): Int = sensorInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Request status
        Log.v("Bind", "$position")
        RxBus.publish(ConnectionEvent(ConnectionEvent.REQUEST_STATUS, sensorInfoList[position]))
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