package com.weisi.tool.wsnbox.permission

import android.app.Activity
import com.weisi.tool.wsnbox.R
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by CJQ on 2018/3/6.
 */
class ReadPermissionsRequester : PermissionsRequester {

    constructor(activity: Activity, requestCode: Int)
        : super(activity, requestCode, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE) {
            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                AppSettingsDialog.Builder(activity).build().show()
            } else {
                notifyPermissionsDenied()
            }
        }
    }

    override fun getRequestRationaleRes(): Int {
        return R.string.grant_read_perimission;
    }
}