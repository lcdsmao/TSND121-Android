package com.paranoid.mao.inertialsensorservice

fun List<Byte>.littleEndianInt(): Int {
    if (this.size > 4) return 0
    return this.fold(0) { acc, byte ->
        (acc shl 8) or (byte.toInt() and 0xFF)
    }
}