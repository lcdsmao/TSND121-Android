package com.paranoid.mao.tsnd121.data.sensor

import android.util.Log
import com.paranoid.mao.tsnd121.vo.AccGyrMagData
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorDataSaver(
        rootFolder: String,
        deviceName: String
) {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    private val folder = File("$rootFolder/$deviceName")
    private val csvFormat = SimpleCsvFormat(10)
    private var filePrinter: PrintWriter? = null
    private var first = Int.MAX_VALUE
    private var last = Int.MIN_VALUE
    private var count = 0
    private val lock = Any()

    private var fileName: String = deviceName
        get() = "${dateFormat.format(Calendar.getInstance().time)}_$field.csv"

    init {
        createFile()
    }

    fun recordData(data: AccGyrMagData) {
        synchronized(lock) {
            filePrinter?.print(csvFormat.format(data.toList()))
            count++
            first = min(data.time, first)
            last = max(data.time, last)
        }
    }

    fun close() {
        filePrinter?.flush()
        filePrinter?.close()
        filePrinter = null
        Log.v(SensorDataSaver::class.java.simpleName, "$fileName: $first, $last, ${last-first}, $count")
        first = Int.MAX_VALUE
        last = Int.MIN_VALUE
        count = 0
    }

    private fun createFile() {
        folder.mkdirs()
        filePrinter = File(folder, fileName).printWriter()
        filePrinter?.print(csvFormat.format(
                listOf("time(msec)", "Ax", "Ay", "Az", "Gx", "Gy", "Gz", "Mx", "My", "Mz")))
    }
}