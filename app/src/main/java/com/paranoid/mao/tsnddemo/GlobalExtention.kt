package com.paranoid.mao.tsnddemo

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Paranoid on 2/1/18.
 */
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}