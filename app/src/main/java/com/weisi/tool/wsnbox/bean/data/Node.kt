package com.weisi.tool.wsnbox.bean.data

import android.os.Parcel
import android.os.Parcelable
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.SensorManager

/**
 * Created by CJQ on 2018/6/8.
 */
class Node(val name: String?, val measurement: DisplayMeasurement<*>) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            SensorManager.getMeasurement(parcel.readLong()) as DisplayMeasurement<*>)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(measurement.id.id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Node> {
        override fun createFromParcel(parcel: Parcel): Node {
            return Node(parcel)
        }

        override fun newArray(size: Int): Array<Node?> {
            return arrayOfNulls(size)
        }
    }
}