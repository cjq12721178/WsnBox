package com.weisi.tool.wsnbox.bean.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by CJQ on 2018/5/29.
 */
class Device(val name: String, val nodes: List<Node>) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.createTypedArrayList(Node)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeTypedList(nodes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device {
            return Device(parcel)
        }

        override fun newArray(size: Int): Array<Device?> {
            return arrayOfNulls(size)
        }
    }
}