package com.paranoid.mao.tsnddemo


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

/**
 * A simple [Fragment] subclass.
 */
class RealtimeGraphFragment : Fragment() {

    private val dataSeriesX = LineGraphSeries<DataPoint>().apply { title = "X"; color = Color.RED }
    private val dataSeriesY = LineGraphSeries<DataPoint>().apply { title = "Y"; color = Color.GREEN }
    private val dataSeriesZ = LineGraphSeries<DataPoint>().apply { title = "Z"; color = Color.BLUE }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_realtime_graph, container, false)

        val graphView = view.findViewById<GraphView>(R.id.graphView)
        graphView.apply {
            addSeries(dataSeriesX)
            addSeries(dataSeriesY)
            addSeries(dataSeriesZ)
            viewport.isXAxisBoundsManual = true
            viewport.isYAxisBoundsManual = true
            viewport.setMinX(0.0)
            viewport.setMaxX(10.0)
            viewport.setMinY(-6.0)
            viewport.setMaxY(6.0)
//            viewport.isScrollable = true
            legendRenderer.isVisible = true
            legendRenderer.align = LegendRenderer.LegendAlign.TOP

        }
        return view
    }

    fun addData(time: Int, dataX: Int, dataY: Int, dataZ: Int) {
        dataSeriesX.appendData(time, dataX)
        dataSeriesY.appendData(time, dataY)
        dataSeriesZ.appendData(time, dataZ)
    }

    private fun LineGraphSeries<DataPoint>.appendData(time: Int, data: Int) {
        appendData(DataPoint(time.toDouble() / 1000.0, data.toDouble() / 10000.0), true,  300)
    }
}
