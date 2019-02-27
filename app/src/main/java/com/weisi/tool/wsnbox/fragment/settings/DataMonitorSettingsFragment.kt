package com.weisi.tool.wsnbox.fragment.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceCategory
import android.preference.SwitchPreference
import android.provider.Settings
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper
import kotlin.properties.Delegates

class DataMonitorSettingsFragment : BaseSettingsFragment() {

    private var notifyParameterCategory by Delegates.notNull<PreferenceCategory>()

    private val enablePreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onPreferenceChange(newValue: Any?): Boolean {
            val enabled = newValue == true
            if (enabled) {
                preferenceActivity.dataPrepareService.startListenDataAlarm(true)
            } else {
                preferenceActivity.dataPrepareService.stopListenDataAlarm()
            }
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
            enableNotifyPreferenceHelper.preference.isEnabled = checked
            notifyParameterCategory.isEnabled = checked
            enableToastPreferenceHelper.preference.isEnabled = checked
        }

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnEnable)
        }
    }

    private val enableNotifyPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnNotifyEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
            notifyParameterCategory.isEnabled = checked
        }
    }

    private val enableNotifySoundPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnNotifySoundEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            preferenceActivity.dataPrepareService.valueAlarmer.enableNotifySound(newValue == true)
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    private val enableNotifyVibratePreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnNotifyVibrateEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            preferenceActivity.dataPrepareService.valueAlarmer.enableNotifyVibrate(newValue == true)
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    private val enableNotifyFloatPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnNotifyFloatEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            preferenceActivity.dataPrepareService.valueAlarmer.enableNotifyFloat(newValue == true)
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    private val enableNotifyScreenPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnNotifyScreenEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            preferenceActivity.dataPrepareService.valueAlarmer.enableNotifyScreen(newValue == true)
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    private val enableToastPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataWarnToastEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    private val enableDataMonitorBackgroundPreferenceHelper = object : SwitchPreferenceHelper() {

        override fun onInitialize(preference: SwitchPreference?) {
            setChecked(settings.isDataMonitorBackgroundEnable)
        }

        override fun onPreferenceChange(newValue: Any?): Boolean {
            return true
        }

        override fun onCheckedChanged(checked: Boolean) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_data_warn)
        setHasOptionsMenu(true)

        settings.notificationParameterCorrect()
        notifyParameterCategory = findPreference(getString(R.string.preference_key_notification_parameter)) as PreferenceCategory
        enableNotifyPreferenceHelper.initialize(this, R.string.preference_key_data_warn_notify_enable)
        enableNotifySoundPreferenceHelper.initialize(this, R.string.preference_key_data_warn_notify_sound_enable)
        enableNotifyVibratePreferenceHelper.initialize(this, R.string.preference_key_data_warn_notify_vibrate_enable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enableNotifyFloatPreferenceHelper.initialize(this, R.string.preference_key_data_warn_notify_float_enable)
            findPreference(getString(R.string.preference_key_data_warn_notify_settings)).setOnPreferenceClickListener {
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", activity.packageName)
                    intent.putExtra("app_uid", activity.applicationInfo.uid)
                }
                startActivity(intent)
                true
            }
            enableNotifyScreenPreferenceHelper.initialize(this, R.string.preference_key_data_warn_notify_screen_enable)
        }
        enableToastPreferenceHelper.initialize(this, R.string.preference_key_data_warn_toast_enable)
        enablePreferenceHelper.initialize(this, R.string.preference_key_data_warn_enable)
        enableDataMonitorBackgroundPreferenceHelper.initialize(this, R.string.preference_key_data_monitor_background_enable)
    }
}