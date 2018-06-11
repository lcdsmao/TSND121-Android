package com.paranoid.mao.tsnd121.ui.graph


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.paranoid.mao.tsnd121.R
import kotlinx.android.synthetic.main.fragment_realtime_graph.*


/**
 * A simple [Fragment] subclass.
 */
class RealtimeGraphFragment : Fragment() {

    private val dataSeriesX = LineGraphSeries<DataPoint>().apply {
        title = "X"
        color = 0xFFF8766D.toInt()
        thickness = 2
    }
    private val dataSeriesY = LineGraphSeries<DataPoint>().apply {
        title = "Y"
        color = 0xFF00B0F6.toInt()
        thickness = 2
    }
    private val dataSeriesZ = LineGraphSeries<DataPoint>().apply {
        title = "Z"
        color = 0xFF6BB100.toInt()
        thickness = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_realtime_graph, container, false)

        val minY = arguments?.getDouble("minY")?: 0.0
        val maxY = arguments?.getDouble("maxY")?: 0.0
        val title = arguments?.getString("title")

        val graphView = view.findViewById<GraphView>(R.id.graphView)
        graphView.apply {
            addSeries(dataSeriesX)
            addSeries(dataSeriesY)
            addSeries(dataSeriesZ)
            setTitle(title)
            viewport.isXAxisBoundsManual = true
            viewport.isYAxisBoundsManual = true
            viewport.setMinX(0.0)
            viewport.setMaxX(5.0)
            viewport.setMinY(minY)
            viewport.setMaxY(maxY)
//            viewport.isScrollable = true
            legendRenderer.isVisible = true
            legendRenderer.align = LegendRenderer.LegendAlign.TOP
            gridLabelRenderer.isHorizontalLabelsVisible = false
        }
        return view
    }

    fun addData(time: Double, dataX: Double, dataY: Double, dataZ: Double) {
        dataSeriesX.appendData(time, dataX)
        dataSeriesY.appendData(time, dataY)
        dataSeriesZ.appendData(time, dataZ)
        data_x.text = dataX.toString()
        data_y.text = dataY.toString()
        data_z.text = dataZ.toString()
    }

    private fun LineGraphSeries<DataPoint>.appendData(time: Double, data: Double) {
        appendData(DataPoint(time, data), true, 55)
    }

    companion object {
        fun newInstance(title: String, minY: Double, maxY: Double): RealtimeGraphFragment {
            val bundle = Bundle()
            bundle.putDouble("minY", minY)
            bundle.putDouble("maxY", maxY)
            bundle.putString("title", title)
            val fragment = RealtimeGraphFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}
