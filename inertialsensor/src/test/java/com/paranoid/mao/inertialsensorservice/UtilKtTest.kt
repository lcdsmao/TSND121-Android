package com.paranoid.mao.inertialsensorservice

import io.reactivex.Flowable
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class UtilKtTest {

    @Test
    fun testShare() {
        val flowable = Flowable.interval(100, TimeUnit.MILLISECONDS).share()
        val d1 = flowable.subscribe({
            println("From 1 $it")
        }, {
            print("Error 1")
        }, {
            print("Complete 1")
        })
        TimeUnit.MILLISECONDS.sleep(300)
        val d2 = flowable
                .timeout(80, TimeUnit.MILLISECONDS)
                .firstOrError()
                .toCompletable()
                .subscribe(
//                        {
//            println("From 2 $it")
//        },
                        {
            println("Error 2")
        }, {
            print("Complete 1")
        })
        TimeUnit.MILLISECONDS.sleep(500)
        d1.dispose()
        TimeUnit.MILLISECONDS.sleep(200)
        d2.dispose()
    }

    @Test
    fun littleEndianInt() {
        assertEquals(0xFF, 255)
        assertEquals(0xFF.toByte(), (-1).toByte())
        val bl1 = listOf<Byte>(0x1A, 0x2B, 0x3C, 0x4D)
        assertEquals(bl1.littleEndianInt(), 1295788826)
        val bl2 = listOf<Byte>(0x1A, 0x2B, 0x3C)
        assertEquals(bl2.littleEndianInt(), 3943194)
        val bl3 = listOf<Byte>(0x1A, 0x2B, 0x80.toByte())
        assertEquals(bl3.littleEndianInt(), -8377574)
    }

    @Test
    fun testByte() {
        assertEquals((-1).toByte(), 0xFF.toByte())
        assertEquals(255.toByte(), 0xFF.toByte())
    }
}