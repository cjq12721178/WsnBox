package com.weisi.tool.wsnbox.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.weisi.tool.wsnbox.R
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MultiplePermissionsRequester(activity: Activity, requestCode: Int, permissions: Array<out String>) : PermissionsRequester(activity, requestCode, permissions) {
    override fun getRequestRationaleRes(): Int {
        return R.string.grant_all_permissions
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE) {
            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // 开启当前应用的权限管理页
                    val packageUri = Uri.parse("package:" + activity.packageName)
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivityForResult(intent, REQUEST_CODE)
                } else {
                    AppSettingsDialog.Builder(activity).build().show()
                }
            } else {
                notifyPermissionsDenied()
            }
        }
    }
}