package com.paranoid.mao.inertialsensorservice

fun List<Byte>.littleEndianInt(): Int {
    if (this.size > 4) return 0
    return this.foldIndexed(0) { index, acc, byte ->
        val t: Int = if (index == lastIndex) {
            byte.toInt()
        } else {
            byte.toInt() and 0xFF
        } shl (index * 8)
        acc or t
    }
}