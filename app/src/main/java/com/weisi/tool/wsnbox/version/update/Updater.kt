package com.weisi.tool.wsnbox.version.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v7.preference.PreferenceManager
import com.cjq.tool.qbox.util.ExceptionLog
import com.weisi.tool.wsnbox.BuildConfig
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.util.UriHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers




class Updater {

    companion object {

        @JvmStatic
        fun checkLatestVersion(context: Context, checkVersionCallBack: CheckVersionCallBack) {
            UpdateServiceFactory()
                    .createServiceFrom(context, UpdateService::class.java)
                    .getUpdateInfo(getChannel())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        checkVersionCallBack.onVersionChecked(it)
                    }, {
                        ExceptionLog.record(it)
                        checkVersionCallBack.onErrorOccurred(it)
                    })
        }

        @JvmStatic
        private fun getChannel(): String {
            val applicationIdSuffix = BuildConfig.APPLICATION_ID.substringAfter("com.weisi.tool.wsnbox.", "")
            return if (applicationIdSuffix.isEmpty() || applicationIdSuffix == "debug") {
                "general"
            } else {
                applicationIdSuffix.substringBefore('.')
            }
        }

        @JvmStatic
        private fun getUpdateUrl(updateInfo: UpdateInfo): String {
            return BuildConfig.SERVICE_SERVER_URL
                    .plus("update/download?channel=")
                    .plus(getChannel())
                    .plus("&apk=")
                    .plus(updateInfo.apkName)
        }

        @JvmStatic
        fun hasNewVersion(context: Context, updateInfo: UpdateInfo) : Boolean {
            return updateInfo.versionCode > context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }

        @JvmStatic
        fun getCurrentDownloadApkId(context: Context) : Long {
            return PreferenceManager.getDefaultSharedPreferences(context).getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        }

        @JvmStatic
        fun update(context: Context, updateInfo: UpdateInfo) {
            // 获取存储ID
            val downloadId = getCurrentDownloadApkId(context)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            if (downloadId != -1L) {
                val status = getDownloadStatus(downloadManager, downloadId)
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        //启动更新界面
                        if (!tryStartInstallActivity(context, downloadManager, downloadId)) {
                            download(context, downloadManager, updateInfo)
                        }
                    }
                    //DownloadManager.STATUS_RUNNING -> SimpleCustomizeToast.show(R.string.apk_downloading)
                    else -> download(context, downloadManager, updateInfo)
                }
            } else {
                download(context, downloadManager, updateInfo)
            }
        }

        @JvmStatic
        fun tryStartInstallActivity(context: Context, downloadManager: DownloadManager, downloadId: Long) : Boolean {
            val downloadApkUri = downloadManager.getUriForDownloadedFile(downloadId)
            return if (downloadApkUri == null) {
                downloadManager.remove(downloadId)
                false
            } else {
                return if (canApkInstall(context, UriHelper.getRealFilePath(context, downloadApkUri))) {
                    startInstallActivity(context, downloadApkUri)
                    true
                } else {
                    false
                }
            }
        }

        /**
         * 获取下载状态
         *
         * @param downloadId an ID for the update, unique across the system.
         * This ID is used to make future calls related to this update.
         * @return int
         * @see DownloadManager.STATUS_PENDING
         *
         * @see DownloadManager.STATUS_PAUSED
         *
         * @see DownloadManager.STATUS_RUNNING
         *
         * @see DownloadManager.STATUS_SUCCESSFUL
         *
         * @see DownloadManager.STATUS_FAILED
         */
        @JvmStatic
        private fun getDownloadStatus(downloadManager: DownloadManager, downloadId: Long): Int {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val c = downloadManager.query(query)
            if (c != null) {
                c.use { c ->
                    if (c.moveToFirst()) {
                        return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    }
                }
            }
            return -1
        }

        /**
         * 下载的apk和当前程序版本比较
         *
         * @param apkPath apk file path
         * @param context Context
         * @return 如果当前应用版本小于apk的版本则返回true
         */
        @JvmStatic
        private fun canApkInstall(context: Context, apkPath: String): Boolean {
            val apkInfo = context.packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES) ?: return false
            if (BuildConfig.APP_DEBUG) {
                return true
            }
            val localPackage = context.packageName
            if (apkInfo.packageName == localPackage) {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(localPackage, 0)
                    if (apkInfo.versionCode > packageInfo.versionCode) {
                        return true
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    ExceptionLog.record(e)
                }
            }
            return false
        }

        @JvmStatic
        private fun download(context: Context, downloadManager: DownloadManager, updateInfo: UpdateInfo) {
            val req = DownloadManager.Request(Uri.parse(getUpdateUrl(updateInfo)))
            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI + DownloadManager.Request.NETWORK_MOBILE)
            //req.setAllowedOverRoaming(false);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            //设置文件的保存的位置[三种方式]
            //第一种
            //file:///storage/emulated/0/Android/data/your-package/files/Download/update.apk
            //req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$appName.apk")
            //第二种
            //file:///storage/emulated/0/Download/update.apk
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, updateInfo.apkName);
            //第三种 自定义文件路径
            //req.setDestinationUri()

            // 设置一些基本显示信息
            req.setTitle(updateInfo.apkName)
            req.setDescription(context.getString(R.string.download_apk_description))
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putLong(DownloadManager.EXTRA_DOWNLOAD_ID, downloadManager.enqueue(req)).commit()
        }

        @JvmStatic
        private fun startInstallActivity(context: Context, uri: Uri) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    interface CheckVersionCallBack {
        fun onVersionChecked(updateInfo: UpdateInfo)
        fun onErrorOccurred(t: Throwable)
    }
}