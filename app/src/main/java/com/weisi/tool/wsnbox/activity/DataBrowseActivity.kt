package com.weisi.tool.wsnbox.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.browse.BaseDataBrowseSensorAdapterDelegate
import com.weisi.tool.wsnbox.bean.configuration.Settings.*
import com.weisi.tool.wsnbox.fragment.browse.DataBrowseFragment
import com.weisi.tool.wsnbox.fragment.browse.DeviceNodeFragment
import com.weisi.tool.wsnbox.fragment.browse.LogicalSensorFragment
import com.weisi.tool.wsnbox.fragment.browse.PhysicalSensorFragment
import com.weisi.tool.wsnbox.fragment.dialog.PhysicalSensorDetailsDialog
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.NullHelper
import kotlinx.android.synthetic.main.activity_data_browse.*

class DataBrowseActivity : BaseActivity(),
        RadioGroup.OnCheckedChangeListener {

    private var switchableFragmentManager: SwitchableFragmentManager<DataBrowseFragment<*, *>> by NullHelper.readonlyNotNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_browse)

        startService(Intent(this, DataPrepareService::class.java))
        //Log.d(Tag.LOG_TAG_D_TEST, "DataBrowseActivity onCreate")
        BaseDataBrowseSensorAdapterDelegate.init(this)
        //初始化显示模式切换管理器
        switchableFragmentManager = SwitchableFragmentManager(supportFragmentManager,
                R.id.fl_view_carrier,
                arrayOf(getString(R.string.physical_sensor_mode),
                        getString(R.string.logical_sensor_mode),
                        getString(R.string.device_node_mode)),
                arrayOf(PhysicalSensorFragment::class.java,
                        LogicalSensorFragment::class.java,
                        DeviceNodeFragment::class.java))
        rg_view_mode.setOnCheckedChangeListener(this)

        chooseViewModeAndShowSensorInfoFromWarnNotification(intent)
    }

    private fun chooseViewModeAndShowSensorInfoFromWarnNotification(newIntent: Intent?) {
        val sensorAddressFromNotification = newIntent?.getIntExtra(Constant.TAG_ADDRESS, 0) ?: 0

        //切换至上一次选择的显示模式
        if (sensorAddressFromNotification != 0) {
            rdo_physical_sensor_mode
        } else {
            when (settings.lastDataBrowseViewMode) {
                VM_LOGICAL_SENSOR -> rdo_logical_sensor_mode
                VM_DEVICE_NODE -> rdo_device_node_mode
                else -> rdo_physical_sensor_mode
            }
        }.isChecked = true

        //由点击告警通知打开相应传感器详细信息对话框
        if (sensorAddressFromNotification != 0) {
            val dialog = PhysicalSensorDetailsDialog()
            dialog.init(SensorManager.getPhysicalSensor(sensorAddressFromNotification))
            dialog.show(supportFragmentManager, DataBrowseFragment.DIALOG_TAG_INFO)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //Log.d(Tag.LOG_TAG_D_TEST, "onNewIntent, address: ${intent?.getIntExtra(Constant.TAG_ADDRESS, 0)}")
        chooseViewModeAndShowSensorInfoFromWarnNotification(intent)
    }

//    override fun onDestroy() {
//        Log.d(Tag.LOG_TAG_D_TEST, "DataBrowseActivity onDestroy")
//        //SensorManager.setValueContainerConfigurationProvider(null, true)
//        super.onDestroy()
//    }

    override fun onBackPressed() {
        if (intent.getIntExtra(Constant.TAG_ADDRESS, 0) != 0
                && isTaskRoot
                && !settings.isDataMonitorBackgroundEnable) {
            stopService(Intent(this, DataPrepareService::class.java))
        }
        super.onBackPressed()
    }

//    private fun importSensorConfigurations() {
//        val id = settings.dataBrowseValueContainerConfigurationProviderId
//        if (id > 0) {
//            SensorConfigurationsChanger(this).execute(id)
//        }
//    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        switchableFragmentManager.switchTo(when (checkedId) {
            R.id.rdo_physical_sensor_mode -> getString(R.string.physical_sensor_mode)
            R.id.rdo_logical_sensor_mode -> getString(R.string.logical_sensor_mode)
            R.id.rdo_device_node_mode -> getString(R.string.device_node_mode)
            else -> throw IllegalArgumentException("change view mode failed")
        })
        settings.lastDataBrowseViewMode = when (checkedId) {
            R.id.rdo_logical_sensor_mode -> VM_LOGICAL_SENSOR
            R.id.rdo_device_node_mode -> VM_DEVICE_NODE
            else -> VM_PHYSICAL_SENSOR
        }
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        service.sensorHistoryDataAccessor.importSensorsWithEarliestValue(null, true)
        super.onServiceConnectionCreate(service)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_data_browse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_sort -> {
                switchableFragmentManager.currentFragment.onSortButtonClick()
            }
            R.id.mi_filter -> {
                switchableFragmentManager.currentFragment.onFilterButtonClick()
            }
            R.id.mi_search -> {
                switchableFragmentManager.currentFragment.onSearchButtonClick()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun onSensorConfigurationChanged() {
        switchableFragmentManager.currentFragment.onSensorConfigurationChanged()
    }

//    override fun onResultAchieved(result: SensorManager.MeasurementConfigurationProvider?) {
//        result?.let {
//            switchableFragmentManager.currentFragment.onSensorConfigurationChanged()
//        }
//    }
}
