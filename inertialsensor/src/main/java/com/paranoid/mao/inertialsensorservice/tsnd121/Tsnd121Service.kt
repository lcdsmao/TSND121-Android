package com.paranoid.mao.inertialsensorservice.tsnd121

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.paranoid.mao.inertialsensorservice.InertialSensorData
import com.paranoid.mao.inertialsensorservice.InertialSensorService
import com.paranoid.mao.inertialsensorservice.littleEndianInt
import io.reactivex.*
import io.reactivex.rxkotlin.toMaybe
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

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
        private val deviceName: String = "",
        private val deviceAddress: String,
        private val samplingInterval: Int = 10,
        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) : InertialSensorService {

    private var bluetoothSocket: BluetoothSocket? = null
    private var connectionFlowable: Flowable<Tsnd121Command> = Flowable.empty()

    override fun connect(): Flowable<InertialSensorData> {
        Log.v("Connect", "Before crate")
        val map = createConnection()
                .filter { response ->
                    //                Log.v("Resonse", response.toString())
                    response.commandCode in arrayOf(
                            Tsnd121CommandCode.RECEIVED_ACC_GYRO_DATA,
                            Tsnd121CommandCode.RECEIVED_MAG_DATA
                    )

                }
                .buffer(2)
                .map {
                    it.sortBy { c -> c.commandCode }
                    //                Log.v("Data Command", it[0].toString())
                    //                Log.v("Data Command", it[1].toString())
                    // 0x80
                    val accGyro = extractData(it[1])
                    // 0x81
                    //                val mag = extractData(it[0])
                    //                InertialSensorData(time = accGyro[0],
                    //                        accX = accGyro[1], accY = accGyro[2], accZ = accGyro[3],
                    //                        gyroX = accGyro[4], gyroY = accGyro[5], gyroZ = accGyro[6],
                    //                        magX = mag[1], magY = mag[2], magZ = mag[3]
                    //                )
                    InertialSensorData()
                }
        return map
    }

    override fun disconnect() = Completable.fromAction {
        beep(BEEP_DISCONNECT)
        bluetoothSocket?.close()
    }!!

    override fun startMeasure(): Completable {
        val startCompletable = connectionFlowable
                .filter {
                    it.commandCode == Tsnd121CommandCode.RECEIVED_START_MEASURING
                }.firstOrError()
//                .timeout(1, TimeUnit.SECONDS, Schedulers.newThread())
                .toCompletable()
        return Completable.fromAction {
            initMeasuring()
        }.andThen(startCompletable)
    }

    override fun stopMeasure(): Completable {
        val stopCompletable = connectionFlowable
                .filter {
                    it.commandCode == Tsnd121CommandCode.RECEIVED_STOP_MEASURING
                }.firstOrError()
//                .timeout(1, TimeUnit.SECONDS, Schedulers.newThread())
                .toCompletable()
        return Completable.fromAction {
            val param: Byte = 0
            sendCommand(Tsnd121CommandCode.COMMAND_STOP_MEASURING, param)
        }.andThen(stopCompletable)
    }

    private fun mapResponse(command: Tsnd121Command) = when(command.commandCode) {
        Tsnd121CommandCode.RECEIVED_ERROR_MEASURING -> {
//            InertialSensorResponse.Error("Measuring Error")
        }
        Tsnd121CommandCode.RECEIVED_GET_BATTERY_CHARGE -> {
            val voltage = command.params.slice(0..1).littleEndianInt()
            val left = command.params.takeLast(1).littleEndianInt()
            val message = "$voltage,$left"
//            InertialSensorResponse.BatteryStatus(message)
        }
        Tsnd121CommandCode.RECEIVED_GET_STATUS -> {
//            InertialSensorResponse.SensorStatus(command.params.toString())
        }
        Tsnd121CommandCode.RECEIVED_COMMAND_RESPONSE -> {
            if (command.params[0] == 0.toByte()) {
//                InertialSensorResponse.Success
            } else {
//                InertialSensorResponse.Error("")
            }
        }
        Tsnd121CommandCode.RECEIVED_START_MEASURING -> {
//            InertialSensorResponse.StartMeasure
        }
        Tsnd121CommandCode.RECEIVED_STOP_MEASURING -> {
//            InertialSensorResponse.StopMeasure
        }
        else -> {
//            InertialSensorResponse.Error("Error")
        }
    }

    private fun beep(vararg param: Byte) {
        sendCommand(Tsnd121CommandCode.COMMAND_SOUND_BEEP, *param)
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
    private fun initMeasuring() {
        sendCommand(Tsnd121CommandCode.COMMAND_ACCGYR_SETTING, samplingInterval.toByte(), 1, 0)
        sendCommand(Tsnd121CommandCode.COMMAND_MAG_SETTING, samplingInterval.toByte(), 1, 0)
        sendCommand(Tsnd121CommandCode.COMMAND_ACC_RANGE_SETTING, samplingInterval.toByte(), 2)
        sendCommand(Tsnd121CommandCode.COMMAND_GYR_RANGE_SETTING, samplingInterval.toByte(), 2)
        sendCommand(Tsnd121CommandCode.COMMAND_START_MEASURING,
                0,
                0, 1, 1,
                0, 0, 0,
                0,
                0, 1, 1,
                0, 0, 0)
    }

    private fun sendCommand(commandCode: Byte, vararg params: Byte) {
//        Log.v("Tsnd121Command", "$commandCode, $params")
        val command = Tsnd121Command(commandCode, params.asList())
        bluetoothSocket?.let {
            it.outputStream.write(command.toByteArray())
            it.outputStream.flush()
        }

    }

    private fun createConnection() = Flowable.create<Tsnd121Command>({ emitter ->
            try {
                val bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
                Log.v("Device", "try")
                bluetoothDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID).let {
                    bluetoothSocket = it
                    Log.v("Create", "Connection")
                    it.connect()

                    beep(BEEP_CONNECT)
                    beep(BEEP_CONNECT)

                    val inputStream = it.inputStream
                    val array = ByteArray(64)
                    while (true) {
                        val header = inputStream.read().toByte()
                        if (header == Tsnd121CommandCode.PROTOCOL_HEADER) {
                            var commandCode: Int
                            do {
                                commandCode = inputStream.read()
                            } while (commandCode == -1)
                            val paramLen = Tsnd121CommandCode.pLenMap[commandCode.toByte()]?: continue
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
                                emitter.onNext(cmd)
                            } else {
                                throw IOException("BCC is incorrect")
                            }
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                emitter.onError(e)
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.ERROR)
            .subscribeOn(Schedulers.io())
            .share()
            .also {
                connectionFlowable = it
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
