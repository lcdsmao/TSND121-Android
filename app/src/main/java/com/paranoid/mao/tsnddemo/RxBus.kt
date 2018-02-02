package com.paranoid.mao.tsnddemo

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Paranoid on 1/29/18.
 */
object RxBus {

    private val publishSubject = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publishSubject.onNext(event)
    }

    fun <T> listen(eventType: Class<T>): Observable<T> = publishSubject.ofType(eventType)
}