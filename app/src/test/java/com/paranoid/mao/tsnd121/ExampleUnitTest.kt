package com.paranoid.mao.tsnd121

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import org.junit.Test

import org.junit.Assert.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDisposable() {
        val ob = Flowable.using({
           "TEST start"
        }, {
           Flowable.create<Int>({
               var i = 0
               while (!it.isCancelled) {
                   try {
                       TimeUnit.MILLISECONDS.sleep(100)
                   } catch (e: InterruptedException) {

                   }
                   it.onNext(i++)
                   if (i == 5) it.onError(IOException())
               }
           }, BackpressureStrategy.BUFFER)
        }, {
           print(it)
        })
                .subscribeOn(Schedulers.io())
                .share()
        val d1 = ob.subscribe(::println, Throwable::printStackTrace, { print("complete")})
        val d2 = ob.subscribe({ println("two $it")})
        TimeUnit.MILLISECONDS.sleep(500)
        d1.dispose()
        TimeUnit.MILLISECONDS.sleep(500)
        d2.dispose()
        TimeUnit.MILLISECONDS.sleep(500)
    }
}
