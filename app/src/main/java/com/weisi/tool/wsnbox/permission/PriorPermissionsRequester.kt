package com.weisi.tool.wsnbox.permission

import android.app.Activity
import android.content.Intent
import com.weisi.tool.wsnbox.R
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class PriorPermissionsRequester : PermissionsRequester {

    constructor(activity: Activity)
            : this(activity, 99) {
    }

    constructor(activity: Activity, requestCode: Int)
            : super(activity, requestCode, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE) {
            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                AppSettingsDialog
                        .Builder(activity)
                        .setRequestCode(REQUEST_CODE)
                        .build()
                        .show()
            } else {
                notifyPermissionsDenied()
            }
        }
    }

    override fun getRequestRationaleRes(): Int {
        return R.string.grant_all_permissions
    }

    override fun onActivityResult(resultCode: Int, data: Intent?) {
        PriorPermissionsRequester(activity, REQUEST_CODE + 1).requestPermissions(object : OnRequestResultListener {
            override fun onPermissionsGranted() {
                this@PriorPermissionsRequester.notifyPermissionsGranted()
            }

            override fun onPermissionsDenied() {
                this@PriorPermissionsRequester.notifyPermissionsDenied()
            }
        })
//        requestPermissions(object : OnRequestResultListener {
//            override fun onPermissionsGranted() {
//                notifyPermissionsGranted()
//            }
//
//            override fun onPermissionsDenied() {
//                notifyPermissionsDenied()
//            }
//        })
    }
}