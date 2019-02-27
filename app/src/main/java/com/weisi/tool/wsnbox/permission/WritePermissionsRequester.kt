package com.weisi.tool.wsnbox.permission

import android.app.Activity
import com.weisi.tool.wsnbox.R

class WritePermissionsRequester(activity: Activity, requestCode: Int) : PermissionsRequester(activity, requestCode, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
    override fun getRequestRationaleRes(): Int {
        return R.string.grant_write_permission
    }
}