package com.paranoid.mao.atrsensorservice.tsnd121

object Tsnd121CommandCode {
    const val PROTOCOL_HEADER = 0x9A.toByte()

    const val COMMAND_GET_INFO = 0x10.toByte()
    const val COMMAND_START_MEASURING = 0x13.toByte()
    const val COMMAND_STOP_MEASURING = 0x15.toByte()
    const val COMMAND_SET_ACCGYR_SETTING = 0x16.toByte()
    const val COMMAND_GET_ACCGYR_SETTING = 0x17.toByte()
    const val COMMAND_SET_MAG_SETTING = 0x18.toByte()
    const val COMMAND_GET_MAG_SETTING = 0x19.toByte()
    const val COMMAND_SET_PRES_SETTING = 0x1A.toByte()
    const val COMMAND_GET_PRES_SETTING = 0x1B.toByte()

    const val COMMAND_SET_ACC_RANGE_SETTING = 0x22.toByte()
    const val COMMAND_GET_ACC_RANGE_SETTING = 0x23.toByte()
    const val COMMAND_SET_ACC_OFFSET = 0x24.toByte()
    const val COMMAND_SET_GYR_RANGE_SETTING = 0x25.toByte()
    const val COMMAND_GET_GYR_RANGE_SETTING = 0x26.toByte()
    const val COMMAND_SET_GYR_OFFSET = 0x27.toByte()
    const val COMMAND_MAG_CALIBRATION_SETTING = 0x28.toByte()

    const val COMMAND_SET_BEEP_VOLUME = 0x32.toByte()
    const val COMMAND_GET_BEEP_VOLUME = 0x33.toByte()
    const val COMMAND_SOUND_BEEP = 0x34.toByte()
    const val COMMAND_GET_BATTERY_CHARGE = 0x3B.toByte()
    const val COMMAND_GET_STATUS = 0x3C.toByte()

    const val RECEIVED_ACC_GYRO_DATA = 0x80.toByte()
    const val RECEIVED_MAG_DATA = 0x81.toByte()
    const val RECEIVED_PRES_DATA = 0x82.toByte()
    const val RECEIVED_ERROR_MEASURING = 0x87.toByte()
    const val RECEIVED_START_MEASURING = 0x88.toByte()
    const val RECEIVED_STOP_MEASURING = 0x89.toByte()

    const val RECEIVED_COMMAND_RESPONSE = 0x8F.toByte()
    const val RECEIVED_INFO = 0x90.toByte()
    const val RECEIVED_ACC_GYRO_SETTING = 0x97.toByte()
    const val RECEIVED_MAG_SETTING = 0x99.toByte()
    const val RECEIVED_PRES_SETTING = 0x9B.toByte()
    const val RECEIVED_GET_BATTERY_CHARGE = 0xBB.toByte()
    const val RECEIVED_GET_STATUS = 0xBC.toByte()

    val pLenMap = mapOf(
            RECEIVED_ACC_GYRO_DATA to 22,
            RECEIVED_MAG_DATA to 13,
            RECEIVED_ERROR_MEASURING to 5,
            RECEIVED_START_MEASURING to 1,
            RECEIVED_STOP_MEASURING to 1,
            RECEIVED_COMMAND_RESPONSE to 1,
            RECEIVED_GET_BATTERY_CHARGE to 3,
            RECEIVED_GET_STATUS to 1
    )
}