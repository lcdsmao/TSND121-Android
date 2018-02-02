package com.paranoid.mao.tsnddemo

import android.annotation.SuppressLint
import android.content.SharedPreferences
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Paranoid on 2/1/18.
 */
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}

@Suppress("UNCHECKED_CAST")
fun <T> SharedPreferences.get(key: String, defaultValue: T): T {
    with(this)
    {
        val result: Any = when (defaultValue) {
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            is String -> getString(key, defaultValue)
            else -> throw IllegalArgumentException()
        }
        return result as T
    }
}

@SuppressLint("CommitPrefEdits")
fun <T> SharedPreferences.put(key: String, value: T) {
    with(this.edit())
    {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            else -> throw IllegalArgumentException()
        }.apply()
    }
}