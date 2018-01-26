package com.paranoid.mao.tsnddemo.db

import android.content.Context

/**
 * Created by Paranoid on 1/25/18.
 */

val Context.database: DbHelper
    get() = DbHelper.getDatabase(this)