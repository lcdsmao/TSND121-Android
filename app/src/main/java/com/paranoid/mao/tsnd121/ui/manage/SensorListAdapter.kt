package com.paranoid.mao.tsnd121.ui.manage

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.vo.Sensor
import kotlinx.android.synthetic.main.sensor_list_item.view.*

/**
 * Created by Paranoid on 1/25/18.
 */
class SensorListAdapter(private val viewModel: ManageViewModel)
    : RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    var sensorList = listOf<Sensor>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val holder = ViewHolder(view)
        view.apply {
            setOnClickListener {
                viewModel.click(sensorList[holder.adapterPosition])
            }
            sensorCheck.setOnClickListener {
                viewModel.enable(sensorList[holder.adapterPosition], (it as CheckBox).isChecked)
            }
        }
        return holder
    }

    override fun getItemCount(): Int = sensorList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sensorList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(info: Sensor) = with(itemView) {
            sensorNameTextView.text = info.name
            sensorMACTextView.text = info.mac
            sensorCheck.visibility = View.VISIBLE
            sensorCheck.isChecked = info.enableStatus
            sensorSwitch.visibility = View.GONE
        }
    }
}