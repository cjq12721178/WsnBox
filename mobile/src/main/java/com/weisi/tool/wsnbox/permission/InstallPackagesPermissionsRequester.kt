package com.weisi.tool.wsnbox.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R

class InstallPackagesPermissionsRequester(activity: Activity, requestCode: Int) : ReadPermissionsRequester(activity, requestCode) {
    override fun getRequestRationaleRes(): Int {
        return R.string.grant_read_permission
    }

    override fun onPreNotifyRequestResultListener(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 判断是否有权限
            val haveInstallPermission = activity.packageManager.canRequestPackageInstalls()
            if (!haveInstallPermission) {
                //权限没有打开则提示用户去手动打开
                // 开启当前应用的权限管理页
                val packageUri = Uri.parse("package:" + activity.packageName)
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
                activity.startActivityForResult(intent, REQUEST_CODE)
                return PREPARE_PENDING
            }
        }
        return PermissionsRequester.PREPARE_SUCCESS
    }

    override fun onRequestPermissions() {
        //权限没有打开则提示用户去手动打开
        // 开启当前应用的权限管理页
        val packageUri = Uri.parse("package:" + activity.packageName)
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (isAllPermissionsGranted(perms)) {
            if (onPreNotifyRequestResultListener() == PermissionsRequester.PREPARE_SUCCESS) {
                notifyPermissionsGranted()
            }
        } else {
            notifyPermissionsDenied()
        }
    }

//    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        if (requestCode == REQUEST_CODE) {
//            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
//                if (perms.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
//                    //权限没有打开则提示用户去手动打开
//                    // 开启当前应用的权限管理页
//                    val packageUri = Uri.parse("package:" + activity.packageName)
//                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    activity.startActivityForResult(intent, REQUEST_CODE)
//                } else {
//                    AppSettingsDialog.Builder(activity).build().show()
//                }
//            } else {
//                notifyPermissionsDenied()
//            }
//        }
//    }

    override fun onActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            notifyPermissionsDenied()
            SimpleCustomizeToast.show(R.string.need_ble_permission)
        } else {
            notifyPermissionsGranted()
        }
    }
}