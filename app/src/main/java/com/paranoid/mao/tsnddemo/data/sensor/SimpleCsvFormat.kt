package com.paranoid.mao.tsnddemo.data.sensor

import java.lang.IllegalArgumentException
import kotlin.math.max

/**
 * Created by Paranoid on 12/26/17.
 */
class SimpleCsvFormat(private val numOfColumns: Int) {
    fun format(list: List<Any>): String {
        if (numOfColumns < list.size) throw IllegalArgumentException("list size too large")
        return list.joinToString(separator = ",") + ",".repeat(max(numOfColumns - list.size - 1, 0)) + '\n'
    }
}