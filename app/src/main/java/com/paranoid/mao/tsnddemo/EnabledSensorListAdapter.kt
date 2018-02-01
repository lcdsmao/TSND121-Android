package com.paranoid.mao.tsnddemo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.activated
import com.paranoid.mao.tsnddemo.events.ConnectionEvent
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.sensor_list_item.view.*

/**
 * Created by Paranoid on 1/25/18.
 */
class EnabledSensorListAdapter(val disposable: CompositeDisposable) : RecyclerView.Adapter<EnabledSensorListAdapter.ViewHolder>() {

    var sensorInfoList: List<SensorInfo> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val holder = ViewHolder(view)

        // Set listener
        val busDisposable = RxBus.listen(ConnectionEvent::class.java)
                .filter { it.command == ConnectionEvent.STATUS && it.info == sensorInfoList[holder.adapterPosition] }
                .map { it.isConnect }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(holder.itemView.activated())
        disposable.add(busDisposable)
        return holder
    }

    override fun getItemCount(): Int = sensorInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Request status
        RxBus.publish(ConnectionEvent(ConnectionEvent.REQUEST_STATUS, sensorInfoList[position]))
        holder.bind(sensorInfoList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(info: SensorInfo) = with(itemView) {
            sensorNameTextView.text = info.name
            sensorMACTextView.text = info.mac
            sensorCheck.visibility = View.GONE
        }
    }
}