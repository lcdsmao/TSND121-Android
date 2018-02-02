package com.paranoid.mao.tsnddemo.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.jakewharton.rxbinding2.view.RxView
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.model.SensorInfo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sensor_list_item.view.*
import org.jetbrains.anko.find

/**
 * Created by Paranoid on 1/25/18.
 */
class AllSensorListAdapter(val sensorInfoList: MutableList<SensorInfo>)
    : RecyclerView.Adapter<AllSensorListAdapter.ViewHolder>() {

    val subject = PublishSubject.create<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
        val holder = ViewHolder(view)
        view.find<CheckBox>(R.id.sensorCheck).setOnClickListener {
            //            modify(holder.adapterPosition, 4, (it as CheckBox).isChecked)
            val position = holder.adapterPosition
            sensorInfoList[position] = sensorInfoList[position].copy(enableStatus = (it as CheckBox).isChecked)
        }
        RxView.clicks(view)
                .subscribe {
                    subject.onNext(holder.adapterPosition)
                }
        return holder
    }

    override fun getItemCount(): Int = sensorInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sensorInfoList[position])
    }

//    fun modify(position: Int, compNum: Int, value: Any) {
//        var (id, name, mac, status) = sensorInfoList[position]
//        when(compNum) {
//            2 -> name = value.toString()
//            3 -> mac = value.toString()
//            4 -> status = value as? Boolean ?: false
//            else -> throw IllegalArgumentException()
//        }
//        sensorInfoList[position] = SensorInfo(id, name, mac, status)
//        notifyItemChanged(position)
//    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(info: SensorInfo) = with(itemView) {
            sensorNameTextView.text = info.name
            sensorMACTextView.text = info.mac
            sensorCheck.visibility = View.VISIBLE
            sensorCheck.isChecked = info.enableStatus
            sensorSwitch.visibility = View.GONE
        }
    }
}