package com.weisi.tool.wsnbox.activity

import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.fragment.demo.DemonstrateFragment
import com.weisi.tool.wsnbox.fragment.demo.IntelligentGasketDemoFragment
import com.weisi.tool.wsnbox.processor.importer.DevicesBatchImporter
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.NullHelper
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class DemonstrationActivity : BaseActivity(),
        SafeAsyncTask.ResultAchiever<List<Device>?, Void> {

    private val DIALOG_TAG_DEVICES_NOT_CONFIG = "dev_not_cfg"
    private val DIALOG_TAG_DEVICES_CONFIG_ERROR = "dev_cfg_err"
    private val FRAGMENT_TAG_DEMO_DELEGATE = "demo_delegate"

    //private var demoDelegate: DemonstrateFragment? = null

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "activity onServiceConnectionCreate")
        NullHelper.ifNullOrNot(supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_DEMO_DELEGATE) as DemonstrateFragment?, {
            importParameterConfigurations()
        }, {
            onDemoFragmentCreate(it)
        })
    }

    private fun importParameterConfigurations() {
        //SensorAndDeviceConfigurationImporter(this)
        DevicesBatchImporter(this)
                .execute(baseApplication.settings.dataBrowseValueContainerConfigurationProviderId)
    }

    private fun displayDemo(devices: List<Device>?) {
        if (devices?.isNotEmpty() == true) {
            if (isIntelligentGasketDemo(devices)) {
                //Log.d(Tag.LOG_TAG_D_TEST, "fragment exists: ${supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_DEMO_DELEGATE) != null}")
                val fragment = IntelligentGasketDemoFragment()
                fragment.initDevices(devices)
                fragment.tag ?: supportFragmentManager
                        .beginTransaction()
                        .add(android.R.id.content, fragment, FRAGMENT_TAG_DEMO_DELEGATE)
                        .commit()
                onDemoFragmentCreate(fragment)
                //demoDelegate = fragment
            } else {
                val dialog = ConfirmDialog()
                dialog.setTitle(R.string.devices_config_error)
                dialog.setDrawCancelButton(false)
                dialog.show(supportFragmentManager, DIALOG_TAG_DEVICES_CONFIG_ERROR)
            }
        } else {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.devices_not_config)
            dialog.setDrawCancelButton(false)
            dialog.show(supportFragmentManager, DIALOG_TAG_DEVICES_NOT_CONFIG)
        }
    }

    private fun onDemoFragmentCreate(fragment: DemonstrateFragment) {
        setTitle(fragment.titleRes)
        //fragment.onServiceConnectionCreate(dataPrepareService)
    }

    private fun isIntelligentGasketDemo(devices: List<Device>): Boolean {
        if (devices.size == 1 && devices[0].nodes.size == 4) {
            for (node in devices[0].nodes) {
                if (node.measurement.id.dataTypeAbsValue != 0x60
                        || node.measurement.configuration.warner !is CommonSingleRangeWarner) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun onConfirm(dialog: BaseDialog<*>): Boolean {
        if (super.onConfirm(dialog)) {
            return true
        }
        when (dialog.tag) {
            DIALOG_TAG_DEVICES_NOT_CONFIG,
            DIALOG_TAG_DEVICES_CONFIG_ERROR -> {
                finish()
            }
        }
        return true
    }

    override fun onResultAchieved(result: List<Device>?) {
        displayDemo(result)
    }
}
