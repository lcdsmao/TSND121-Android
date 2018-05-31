package com.paranoid.mao.inertialsensorservice.tsnd121

import kotlin.experimental.xor

/**
* Tsnd121Command Format:
* |----------------------------------------------------------|
* | Offset   | +0        | +1        | +2~+(n+1) | +(n+2)    |
* |----------------------------------------------------------|
* | Data     | Header    | Command   | Parameter | BCC       |
* | Type     | (0x9A)    | Code      | (n=1~246) |           |
*
* Header: Constant 0x9A
* Command Code: Command
* Parameter: Length is varied. Little endian (1234ABCD -> [CD AB 34 12])
* BCC: The XOR value between Offset+0 ~ Offset+(n+1)
*/

data class Tsnd121Command(val commandCode: Byte, val params: List<Byte>) {
    private val header = Tsnd121CommandCode.PROTOCOL_HEADER
    val bcc = header xor commandCode xor params.reduce(Byte::xor)

    fun toByteArray() = byteArrayOf(header, commandCode, *params.toByteArray(), bcc)
}