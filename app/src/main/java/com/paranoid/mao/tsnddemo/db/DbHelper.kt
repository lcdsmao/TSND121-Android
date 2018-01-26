package com.paranoid.mao.tsnddemo.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.anko.db.*

/**
 * Created by Paranoid on 1/25/18.
 */
class DbHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "WSDatabase", null, 1) {

    companion object {

        private var instance: DbHelper? = null

        @Synchronized
        fun getDatabase(ctx: Context): DbHelper {
            if (instance == null) {
                instance = DbHelper(ctx)
            }
            Log.v(this.toString(), "${instance == null}")
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(DbEntry.SENSOR_DATABASE, true,
                DbEntry.SENSOR_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                DbEntry.SENSOR_NAME to TEXT,
                DbEntry.SENSOR_MAC to TEXT,
                DbEntry.SENSOR_STATUS to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(DbEntry.SENSOR_NAME, true)
    }
}