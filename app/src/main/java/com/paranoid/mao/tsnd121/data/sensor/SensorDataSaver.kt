package com.paranoid.mao.tsnd121.data.sensor

import com.paranoid.mao.tsnd121.vo.AccGyrMagData
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorDataSaver(name: String = "") {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    private val folder = File("sdcard/TSND121/$name")
    private val csvFormat = SimpleCsvFormat(10)
    private var filePrinter: PrintWriter? = null

    private var fileName: String = name
        get() = "${dateFormat.format(Calendar.getInstance().time)}_$field.csv"

    init {
        createFile()
    }

    fun recordData(data: AccGyrMagData) {
        filePrinter?.print(csvFormat.format(data.toList()))
    }

    fun close() {
        filePrinter?.flush()
        filePrinter?.close()
        filePrinter = null
    }

    private fun createFile() {
        folder.mkdirs()
        filePrinter = File(folder, fileName).printWriter()
        filePrinter?.print(csvFormat.format(
                listOf("time(msec)", "Ax", "Ay", "Az", "Gx", "Gy", "Gz", "Mx", "My", "Mz")))
    }
}