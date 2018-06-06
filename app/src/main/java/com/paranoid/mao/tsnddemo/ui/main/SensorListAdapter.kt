package com.paranoid.mao.tsnddemo.ui.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.vo.Sensor
import kotlinx.android.synthetic.main.sensor_list_item.view.*

/**
 * Created by Paranoid on 1/25/18.
 */
class SensorListAdapter(private val viewModel: SensorControlViewModel)
    : RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    var sensorList: List<Sensor> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val holder = ViewHolder(view)
        with(view) {
            // switch
            sensorSwitch.setOnClickListener {
                val sensor = sensorList[holder.adapterPosition]
                if ((it as Switch).isChecked) {
                    viewModel.connect(sensor)
                } else {
                    viewModel.disConnect(sensor)
                }
            }
        }
        return holder
    }

    override fun getItemCount(): Int = sensorList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = sensorList[position]
        holder.bind(sensor, viewModel.isSensorConnected(sensor), viewModel.isSensorMeasuring(sensor))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(sensor: Sensor, isConnected: Boolean, isMeasuring: Boolean) = itemView.apply {
            sensorNameTextView.text = sensor.name
            sensorMACTextView.text = sensor.mac
            sensorCheck.visibility = View.GONE
            sensorSwitch.apply {
                visibility = View.VISIBLE
                isEnabled = !isMeasuring
                isChecked = isConnected
            }
            sensorStatusView.apply {
                isActivated = isMeasuring
            }
        }
    }
}