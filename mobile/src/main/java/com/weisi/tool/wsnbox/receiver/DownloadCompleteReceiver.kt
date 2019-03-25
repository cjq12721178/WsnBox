package com.weisi.tool.wsnbox.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.version.update.Updater


class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (downloadApkId == Updater.getCurrentDownloadApkId(context)) {
            if (!Updater.tryStartInstallActivity(context, context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager, downloadApkId)) {
                Toast.makeText(context, R.string.try_start_install_activity_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
