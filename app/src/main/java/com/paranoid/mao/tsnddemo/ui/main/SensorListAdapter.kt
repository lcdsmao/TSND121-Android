package com.paranoid.mao.tsnddemo.ui.main

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.vo.Sensor
import kotlinx.android.synthetic.main.sensor_list_item.view.*

/**
 * Created by Paranoid on 1/25/18.
 */
class SensorListAdapter(
        private val viewModel: SensorControlViewModel,
        private val onItemClick: (Sensor) -> Unit)
    : RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    var sensorList: List<Pair<Sensor, AtrSensorStatus>> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val holder = ViewHolder(view)
        with(view) {
            // switch
            sensorSwitch.setOnClickListener {
                val sensor = sensorList[holder.adapterPosition].first
                it.isEnabled = false
                if ((it as Switch).isChecked) {
                    viewModel.connect(sensor)
                } else {
                    viewModel.disconnect(sensor)
                }
            }
            this.setOnClickListener {
                val sensor = sensorList[holder.adapterPosition].first
                onItemClick(sensor)
            }
        }
        return holder
    }

    override fun getItemCount(): Int = sensorList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensorAndStatus = sensorList[position]
        holder.bind(sensorAndStatus)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(sensorAndStatus: Pair<Sensor, AtrSensorStatus>) {
            val (sensor, status) = sensorAndStatus
            itemView.apply {
                sensorNameTextView.text = sensor.name
                sensorMACTextView.text = sensor.mac
                sensorCheck.visibility = View.GONE
                sensorSwitch.apply {
                    isEnabled = true
                    visibility = View.VISIBLE
                    isEnabled = status != AtrSensorStatus.MEASURING
                    isChecked = status != AtrSensorStatus.OFFLINE
                }
                sensorStatusView.apply {
                    isActivated = status == AtrSensorStatus.MEASURING
                }
            }
        }
    }
}