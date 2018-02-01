package com.paranoid.mao.tsnddemo.tsnd

import com.paranoid.mao.tsnddemo.csv.SimpleCSVFormat
import com.paranoid.mao.tsnddemo.model.SensorData
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Paranoid on 12/26/17.
 */
class SensorDataSaver(name: String = "") {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    private val folder = File("sdcard/TSNDDemo/")
    private val csvFormat = SimpleCSVFormat(10)

    private var fileName: String = name
          get() = "${dateFormat.format(Calendar.getInstance().time)}_$field.csv"
    private var sensorDataFile: File? = null

    init {
        createFile()
    }

    @Synchronized
    fun recordData(data: SensorData) {
        sensorDataFile?.appendText(csvFormat.format(data.toList()))
    }

    private fun createFile() {
        folder.mkdirs()
        sensorDataFile = File(folder, fileName)
        sensorDataFile?.appendText(csvFormat.format(
                listOf("time(msec)", "Ax", "Ay", "Az", "Gx", "Gy", "Gz", "Mx", "My", "Mz")))
    }
}