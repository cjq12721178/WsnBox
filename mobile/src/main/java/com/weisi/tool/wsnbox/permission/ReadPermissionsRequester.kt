package com.weisi.tool.wsnbox.permission

import android.app.Activity
import com.weisi.tool.wsnbox.R

/**
 * Created by CJQ on 2018/3/6.
 */
open class ReadPermissionsRequester(activity: Activity, requestCode: Int) : PermissionsRequester(activity, requestCode, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

    //    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        if (requestCode == REQUEST_CODE) {
//            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
//                AppSettingsDialog.Builder(activity).build().show()
//            } else {
//                notifyPermissionsDenied()
//            }
//        }
//    }

    override fun getRequestRationaleRes(): Int {
        return R.string.grant_read_permission
    }
}