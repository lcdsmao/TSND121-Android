package com.paranoid.mao.atrsensorservice.tsnd121

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import com.paranoid.mao.atrsensorservice.AccGyroMagData
import com.paranoid.mao.atrsensorservice.AtrSensorService
import com.paranoid.mao.atrsensorservice.AtrSensorStatus
import com.paranoid.mao.atrsensorservice.littleEndianInt
import io.reactivex.*
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.withLock

//
///**
// * CMD-SPEC
// * https://www.atr-p.com/products/pdf/TSND121-cmd-spec.pdf
// *
// * ServiceStatus:
// * IDLE - COMMAND - ONLINE_MEASURE
// *
// * Tsnd121Command Format:
// * |----------------------------------------------------------|
// * | Offset   | +0        | +1        | +2~+(n+1) | +(n+2)    |
// * |----------------------------------------------------------|
// * | Data     | Header    | Command   | Parameter | BCC       |
// * | Type     | (0x9A)    | Code      | (n=1~246) |           |
// * |----------------------------------------------------------|
// *
// * Header: Constant 0x9A
// * Command Code: Tsnd121Command
// * Parameter: Length is varied. Little endian (1234ABCD -> [CD AB 34 12])
// * BCC: The XOR value between Offset+0 ~ Offset+(n+1)
// *
// * Command Type:
// * Three types
// * Command, Response, Event Command
// *
// * @param samplingInterval: ms
// */
class Tsnd121Service(
        val deviceName: String = "",
        val deviceAddress: String,
        val samplingInterval: Int = 10,
        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) : AtrSensorService {

    private val commandLock = ReentrantLock()
    private val commandProcessor: PublishProcessor<Tsnd121Command> = PublishProcessor.create()
    private val statusProcessor: BehaviorProcessor<AtrSensorStatus> = BehaviorProcessor.createDefault(AtrSensorStatus.OFFLINE)

    override val status: Flowable<AtrSensorStatus>
        get() = statusProcessor

    override val sensorData: Flowable<AccGyroMagData>
        get() = commandProcessor
                .filter {
                    it.commandCode in arrayOf(
                            Tsnd121CommandCode.RECEIVED_ACC_GYRO_DATA,
                            Tsnd121CommandCode.RECEIVED_MAG_DATA
                    )
                }
                .buffer(2)
                .filter {
                    it[0].commandCode != it[1].commandCode
                }
                .map {
                    it.sortBy { c -> c.commandCode }
                    // 0x80
                    val accGyro = extractData(it[0])
                    // 0x81
                    val mag = extractData(it[1])
                    AccGyroMagData(time = accGyro[0],
                            accX = accGyro[1], accY = accGyro[2], accZ = accGyro[3],
                            gyroX = accGyro[4], gyroY = accGyro[5], gyroZ = accGyro[6],
                            magX = mag[1], magY = mag[2], magZ = mag[3]
                    )
                }

    private var bluetoothSocket: BluetoothSocket? = null

    override fun connect() {
        launch {
            try {
                bluetoothAdapter.getRemoteDevice(deviceAddress)
                        .createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                        .let {
                            bluetoothSocket = it
                            it.connect()
                            beep(BEEP_CONNECT)
                            statusProcessor.onNext(AtrSensorStatus.COMMAND)
                            while (it.isConnected) {
                                handleInputCommand(it.inputStream)?.let { cmd ->
                                    if (cmd.commandCode == Tsnd121CommandCode.RECEIVED_START_MEASURING) {
                                        statusProcessor.onNext(AtrSensorStatus.MEASURING)
                                    } else if (cmd.commandCode == Tsnd121CommandCode.RECEIVED_STOP_MEASURING) {
                                        statusProcessor.onNext(AtrSensorStatus.COMMAND)
                                    }
                                    commandProcessor.onNext(cmd)
                                }
                            }
                        }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                statusProcessor.onNext(AtrSensorStatus.OFFLINE)
            }
        }
    }

    private fun handleInputCommand(inputStream: InputStream): Tsnd121Command? {
        val array = ByteArray(64)
        val header = inputStream.read().toByte()
        return if (header == Tsnd121CommandCode.PROTOCOL_HEADER) {
            var commandCode: Int
            do {
                commandCode = inputStream.read()
            } while (commandCode == -1)
            val paramLen = Tsnd121CommandCode.pLenMap[commandCode.toByte()] ?: return null
            var offset = 0
            do {
                val size = inputStream.read(array, offset, paramLen - offset)
                offset += size
            } while (offset < paramLen)
            val cmd = Tsnd121Command(commandCode.toByte(), array.slice(0 until paramLen))
            var bcc: Int
            do {
                bcc = inputStream.read()
            } while (bcc == -1)
            if (bcc.toByte() == cmd.bcc) {
                cmd
            } else {
                null
            }
        } else {
            null
        }
    }

    override fun disconnect() {
        launch {
            if (statusProcessor.value != AtrSensorStatus.OFFLINE) {
                try {
                    beep(BEEP_DISCONNECT)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    bluetoothSocket?.close()
                    statusProcessor.onNext(AtrSensorStatus.OFFLINE)
                }
            }
        }
    }

    override fun startMeasure() {
        launch {
            if (statusProcessor.value == AtrSensorStatus.COMMAND) {
                try {
                    initMeasureSetting()
                    startMeasureNow()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun stopMeasure() {
        launch {
            if (statusProcessor.value == AtrSensorStatus.MEASURING) {
                try {
                    stopMeasureNow()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun beep(param: Byte) {
        sendCommand(Tsnd121CommandCode.COMMAND_SOUND_BEEP, param)
    }

    /**
     * ACC/GYRO:
     * Parameter size = 3
     * Cycle: 0~255 ms
     * Send average turn: 0~255
     * Record average turn: 0~255
     *
     * Command: 0 OK, 1 ERROR
     *
     * Range:
     * 0:±2G,1:±4G,2:±8G,3:±16G
     * 0:±250dps,1:±500dps,2:±1000dps,3:±2000dp
     *
     *
     * MAGNETIC
     * Parameter size = 3
     * Cycle: 0~255 (0, 10~255ms)
     * Send average turn: 0~255
     * Record average turn: 0~255
     *
     * Command: 0 OK, 1 ERROR
     * Calibration: 0
     */
    private fun initMeasureSetting() {
        sendCommand(Tsnd121CommandCode.COMMAND_SET_ACCGYR_SETTING, samplingInterval.toByte(), 1, 0)
        sendCommand(Tsnd121CommandCode.COMMAND_SET_MAG_SETTING, samplingInterval.toByte(), 1, 0)
        sendCommand(Tsnd121CommandCode.COMMAND_SET_PRES_SETTING, 0, 0, 0)
        sendCommand(Tsnd121CommandCode.COMMAND_SET_ACC_RANGE_SETTING, samplingInterval.toByte(), 2)
        sendCommand(Tsnd121CommandCode.COMMAND_SET_GYR_RANGE_SETTING, samplingInterval.toByte(), 2)
    }

    private fun startMeasureNow() {
        sendCommand(Tsnd121CommandCode.COMMAND_START_MEASURING,
                0,
                0, 1, 1,
                0, 0, 0,
                0,
                0, 1, 1,
                0, 0, 0)
    }

    private fun stopMeasureNow() {
        sendCommand(Tsnd121CommandCode.COMMAND_STOP_MEASURING, 0)
    }

    fun calibrateAcc(linearAccMode: Boolean = false, gravityAxis: String? = null): Single<Boolean> {
        val params = if (linearAccMode) {
            byteArrayOf(1, 1, 1, 0, 0, 0)
        } else {
            when(gravityAxis) {
                "X" -> byteArrayOf(2, 1, 1, 0, 0, 0)
                "Y" -> byteArrayOf(1, 2, 1, 0, 0, 0)
                "Z" -> byteArrayOf(1, 1, 2, 0, 0, 0)
                else -> throw IllegalArgumentException()
            }
        }
        return Completable.fromAction {
            sendCommand(Tsnd121CommandCode.COMMAND_SET_ACC_OFFSET, *params)
        }.andThen(createCommandResponse())
                .subscribeOn(Schedulers.io())
    }

    fun calibrateGyro(): Single<Boolean> {
        val params = byteArrayOf(1, 1, 1, 0, 0, 0)
        return Completable.fromAction {
            sendCommand(Tsnd121CommandCode.COMMAND_SET_GYR_OFFSET, *params)
        }.andThen(createCommandResponse())
                .subscribeOn(Schedulers.io())
    }

    fun calibrateMag(): Single<Boolean> {
        val param = 0.toByte()
        return Completable.fromAction {
            sendCommand(Tsnd121CommandCode.COMMAND_MAG_CALIBRATION_SETTING, param)
        }.andThen(createCommandResponse())
                .subscribeOn(Schedulers.io())
    }

    private fun createCommandResponse(): Single<Boolean> {
        return commandProcessor
                .filter { it.commandCode == Tsnd121CommandCode.RECEIVED_COMMAND_RESPONSE }
                .timeout(100, TimeUnit.MILLISECONDS)
                .firstOrError()
                .map {
                    it.params[0] == 0.toByte()
                }
    }

    private fun sendCommand(commandCode: Byte, vararg params: Byte) {
        commandLock.withLock {
            bluetoothSocket?.let {
                if (!it.isConnected) return
                try {
                    val command = Tsnd121Command(commandCode, params.asList())
                    it.outputStream.write(command.toByteArray())
                    it.outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Extract data of acc, gyro, magnetic
     * acc -160000~160000(0.1mg)
     * gyro -200000~200000(0.01dps)
     * mag -12000~12000(0.1uT)
     */
    private fun extractData(response: Tsnd121Command): List<Int> {
        val params = response.params
        val list = ArrayList<Int>()
        // time
        list += params.subList(0, 4).littleEndianInt()
        // raw data
        list += params.subList(4, params.size).windowed(3, 3) {
            it.littleEndianInt()
        }
        return list
    }

    companion object {
        val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")!!

        const val BEEP_CONNECT = 0x02.toByte()
        const val BEEP_DISCONNECT = 0x07.toByte()
    }

}
