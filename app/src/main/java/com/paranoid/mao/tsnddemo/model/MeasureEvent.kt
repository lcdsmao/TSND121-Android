package com.paranoid.mao.tsnddemo.model

/**
 * Created by Paranoid on 2/1/18.
 */
data class MeasureEvent(val command: Command,
                        val isAnyMeasuring: Boolean = false)