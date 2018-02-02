package com.paranoid.mao.tsnddemo.data

/**
 * Created by Paranoid on 1/24/18.
 */
import android.content.Context
import android.content.SharedPreferences
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.get
import com.paranoid.mao.tsnddemo.put
import kotlin.reflect.KProperty

class DelegatedPreferences<T>(private val context: Context, private val key: String, private val defaultValue: T) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return prefs.get(key, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        prefs.put(key, defaultValue)
    }
}