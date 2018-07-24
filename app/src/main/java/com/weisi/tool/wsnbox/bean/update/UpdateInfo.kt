package com.weisi.tool.wsnbox.bean.update

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateInfo(val apkName: String,
                      val versionName: String,
                      val versionCode: Int,
                      val forceUpdate: Boolean,
                      val versionDescription: String) : Parcelable {
}