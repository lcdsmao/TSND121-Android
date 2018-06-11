package com.paranoid.mao.tsnd121.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Paranoid on 1/25/18.
 */
@Entity
@Parcelize
data class Sensor(
        val name: String,
        val mac: String,
        @ColumnInfo(name = "enable_status") val enableStatus: Boolean = false,
        @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    companion object {
        val DUMMY = Sensor("", "", false, 0)
    }
}