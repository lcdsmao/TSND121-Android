package com.paranoid.mao.tsnddemo.service

import com.paranoid.mao.atrsensorservice.AccGyroMagData
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorDataSaver(name: String = "") {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    private val folder = File("sdcard/TSNDDemo/$name")
    private val csvFormat = SimpleCsvFormat(10)
    private lateinit var filePrinter: PrintWriter

    private var fileName: String = name
        get() = "${dateFormat.format(Calendar.getInstance().time)}_$field.csv"

    init {
        createFile()
    }

    fun recordData(data: AccGyroMagData) {
        filePrinter.print(csvFormat.format(data.toList()))
    }

    fun close() {
        filePrinter.flush()
        filePrinter.close()
    }

    private fun createFile() {
        folder.mkdirs()
        filePrinter = File(folder, fileName).printWriter()
        filePrinter.print(csvFormat.format(
                listOf("time(msec)", "Ax", "Ay", "Az", "Gx", "Gy", "Gz", "Mx", "My", "Mz")))
    }
}