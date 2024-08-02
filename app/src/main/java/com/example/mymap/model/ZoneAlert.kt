package com.example.mymap.model

import android.os.Parcel
import android.os.Parcelable

data class ZoneAlert(
    val zoneName: String,
    val status: String,
    val onEnter: Boolean,
    val onLeave: Boolean,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(zoneName)
        parcel.writeString(status)
        parcel.writeByte(if (onEnter) 1 else 0)
        parcel.writeByte(if (onLeave) 1 else 0)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(radius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZoneAlert> {
        override fun createFromParcel(parcel: Parcel): ZoneAlert {
            return ZoneAlert(parcel)
        }

        override fun newArray(size: Int): Array<ZoneAlert?> {
            return arrayOfNulls(size)
        }
    }
}