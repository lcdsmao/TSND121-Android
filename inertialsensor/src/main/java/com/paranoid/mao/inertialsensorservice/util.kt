package com.paranoid.mao.inertialsensorservice

fun List<Byte>.littleEndianInt(): Int {
    if (this.size > 4) return 0
    val base = 8
    var value = 0
    this.forEachIndexed { index, byte ->
        value = value or ((byte.toInt() and 0xFF) shl (base * value))
    }
    return value
}