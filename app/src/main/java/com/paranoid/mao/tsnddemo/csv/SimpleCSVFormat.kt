package com.paranoid.mao.tsnddemo.csv

import java.util.*

/**
 * Created by Paranoid on 12/26/17.
 */
class SimpleCSVFormat(private val numOfColumns: Int) {
    fun format(list: List<*>): String {
        val sb = StringBuilder()
        for (i in 0 until numOfColumns) {
            val str = if (i < numOfColumns) list[i].toString() else ""
            val split = if (i < numOfColumns - 1) "," else "\n"
            sb.append(str + split)
        }
        return sb.toString()
    }
}