package com.weisi.tool.wsnbox.processor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.Warner
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.activity.DataBrowseActivity
import com.weisi.tool.wsnbox.bean.configuration.Settings
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.util.NullHelper

class ValueAlarmer(context: Context, val settings: Settings) : Sensor.OnValueAlarmListener {

    //private val channelId = "warn1"
    private val applicationContext = context.applicationContext
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    var enableNotify = settings.isDataWarnNotifyEnable
//    var enableSound = settings.isDataWarnNotifySoundEnable
//    var enableToast = settings.isDataWarnToastEnable

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NullHelper.doNull(notificationManager.getNotificationChannel(settings.warnNotificationChannelId)) {
                createNotificationChannel(settings.warnNotificationChannelId,
                        settings.isDataWarnNotifySoundEnable,
                        settings.isDataWarnNotifyVibrateEnable,
                        settings.isDataWarnNotifyFloatEnable,
                        settings.isDataWarnNotifyScreenEnable)
            }
        }
        Sensor.setOnValueAlarmListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String,
                                          enableNotifySound: Boolean,
                                          enableNotifyVibrate: Boolean,
                                          enableNotifyFloat: Boolean,
                                          enableNotifyScreen: Boolean) {
        val channel = NotificationChannel(id,
                applicationContext.getString(R.string.warn_info),
                if (enableNotifyFloat) {
                    NotificationManager.IMPORTANCE_HIGH
                } else {
                    NotificationManager.IMPORTANCE_DEFAULT
                })
        if (!enableNotifySound) {
            channel.setSound(null, null)
        }
        channel.enableVibration(enableNotifyVibrate)
        channel.lockscreenVisibility = if (enableNotifyScreen) {
            NotificationCompat.VISIBILITY_PUBLIC
        } else {
            NotificationCompat.VISIBILITY_PRIVATE
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun stop() {
        Sensor.setOnValueAlarmListener(null)
    }

    fun enableNotifySound(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resetNotificationChannel(enabled,
                    settings.isDataWarnNotifyVibrateEnable,
                    settings.isDataWarnNotifyFloatEnable,
                    settings.isDataWarnNotifyScreenEnable)
        }
    }

    fun enableNotifyVibrate(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resetNotificationChannel(settings.isDataWarnNotifySoundEnable,
                    enabled,
                    settings.isDataWarnNotifyFloatEnable,
                    settings.isDataWarnNotifyScreenEnable)
        }
    }

    fun enableNotifyFloat(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resetNotificationChannel(settings.isDataWarnNotifySoundEnable,
                    settings.isDataWarnNotifyVibrateEnable,
                    enabled,
                    settings.isDataWarnNotifyScreenEnable)
        }
    }

    fun enableNotifyScreen(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resetNotificationChannel(settings.isDataWarnNotifySoundEnable,
                    settings.isDataWarnNotifyVibrateEnable,
                    settings.isDataWarnNotifyFloatEnable,
                    enabled)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetNotificationChannel(enableNotifySound: Boolean,
                                         enableNotifyVibrate: Boolean,
                                         enableNotifyFloat: Boolean,
                                         enableNotifyScreen: Boolean) {
        notificationManager.deleteNotificationChannel(settings.warnNotificationChannelId)
        createNotificationChannel(settings.newWarnNotificationChannelId,
                enableNotifySound,
                enableNotifyVibrate,
                enableNotifyFloat,
                enableNotifyScreen)
    }

    override fun onValueTestResult(info: Sensor.Info, measurement: PracticalMeasurement, value: DisplayMeasurement.Value, warnResult: Int) {
        if (warnResult != Warner.RESULT_NORMAL) {
            if (settings.isDataWarnNotifyEnable) {
                val intent = Intent(applicationContext, DataBrowseActivity::class.java)
                intent.putExtra(Constant.TAG_ADDRESS, info.id.address)
                val notification = NotificationCompat.Builder(applicationContext, settings.warnNotificationChannelId)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(applicationContext.getString(R.string.notice_title_sensor_data_abnormal,
                                info.name,
                                info.id.formatAddress))
                        .setContentText(applicationContext.getString(R.string.notice_text_info,
                                measurement.name,
                                measurement.decorateValue(value),
                                applicationContext.getString(when (warnResult) {
                                    DisplayMeasurement.SingleRangeWarner.RESULT_ABOVE_HIGH_LIMIT -> R.string.above_high_limit
                                    DisplayMeasurement.SingleRangeWarner.RESULT_BELOW_LOW_LIMIT -> R.string.below_low_limit
                                    DisplayMeasurement.SwitchWarner.RESULT_ABNORMAL -> R.string.abnormal
                                    else -> throw IllegalArgumentException("normal value may not be warned")
                                })))
                        .setPriority(if (settings.isDataWarnNotifyFloatEnable) {
                            NotificationCompat.PRIORITY_HIGH
                        } else {
                            NotificationCompat.PRIORITY_DEFAULT
                        })
                        .setDefaults(if (settings.isDataWarnNotifySoundEnable) {
                            NotificationCompat.DEFAULT_SOUND
                        } else {
                            0
                        } or if (settings.isDataWarnNotifyVibrateEnable) {
                            NotificationCompat.DEFAULT_VIBRATE
                        } else {
                            0
                        })
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(PendingIntent.getActivity(applicationContext, info.id.address, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        //.setContentIntent(PendingIntent.getBroadcast(applicationContext, info.id.address, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        .build()
                notificationManager.notify(info.id.address, notification)
            }
        }
    }
}