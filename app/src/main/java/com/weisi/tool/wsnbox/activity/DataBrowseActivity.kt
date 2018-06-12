package com.weisi.tool.wsnbox.activity

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SearchDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.configuration.Settings.*
import com.weisi.tool.wsnbox.bean.warner.executor.browse.ViewBackgroundSingleRangeWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.executor.browse.ViewBackgroundSwitchWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import com.weisi.tool.wsnbox.fragment.DataBrowseFragment
import com.weisi.tool.wsnbox.fragment.DeviceNodeFragment
import com.weisi.tool.wsnbox.fragment.LogicalSensorFragment
import com.weisi.tool.wsnbox.fragment.PhysicalSensorFragment
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.Tag
import kotlinx.android.synthetic.main.activity_data_browse.*

class DataBrowseActivity : BaseActivity(),
        RadioGroup.OnCheckedChangeListener {

    //private var realTime = true
    private var sortDialog: SortDialog? = null
    private var filterDialog: FilterDialog? = null
    private var searchDialog: SearchDialog? = null
    var warnProcessor: CommonWarnProcessor<View>? = null
    private lateinit var switchableFragmentManager: SwitchableFragmentManager<DataBrowseFragment<*, *>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_browse)

        Log.d(Tag.LOG_TAG_D_TEST, "onCreate activity")

        //初始化显示模式切换管理器
        switchableFragmentManager = SwitchableFragmentManager(supportFragmentManager,
                R.id.fl_view_carrier,
                arrayOf(getString(R.string.physical_sensor_mode),
                        getString(R.string.logical_sensor_mode),
                        getString(R.string.device_node_mode)),
                arrayOf(PhysicalSensorFragment::class.java,
                        LogicalSensorFragment::class.java,
                        DeviceNodeFragment::class.java))

        //baseApplication.settings.clearLastDataBrowseViewMode()
        //切换至上一次选择的显示模式
        rg_view_mode.setOnCheckedChangeListener(this)
        findViewById<RadioButton>(when (baseApplication.settings.lastDataBrowseViewMode) {
            VM_LOGICAL_SENSOR -> R.id.rdo_logical_sensor_mode
            VM_DEVICE_NODE -> R.id.rdo_device_node_mode
            else -> R.id.rdo_physical_sensor_mode
        }).isChecked = true;

        //导入传感器配置
        importSensorConfigurations()
//        if (savedInstanceState == null) {
//            importSensorConfigurations()
//        }
    }

    override fun onDestroy() {
        SensorManager.setValueContainerConfigurationProvider(null, true)
        super.onDestroy()
    }

    private fun importSensorConfigurations() {
        val id = baseApplication.settings.dataBrowseValueContainerConfigurationProviderId
        if (id > 0) {
            val task = ImportSensorConfigurationsTask()
            task.execute(id)
        }
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        switchableFragmentManager.currentFragment.onServiceConnectionCreate(service)
    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
        switchableFragmentManager.currentFragment.onServiceConnectionStart(service)
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        switchableFragmentManager.currentFragment.onServiceConnectionStop(service)
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        switchableFragmentManager.currentFragment.onServiceConnectionDestroy(service)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        switchableFragmentManager.switchTo(when (checkedId) {
            R.id.rdo_physical_sensor_mode -> getString(R.string.physical_sensor_mode)
            R.id.rdo_logical_sensor_mode -> getString(R.string.logical_sensor_mode)
            R.id.rdo_device_node_mode -> getString(R.string.device_node_mode)
            else -> throw IllegalArgumentException("change view mode failed")
        })
        baseApplication.settings.lastDataBrowseViewMode = when (checkedId) {
            R.id.rdo_logical_sensor_mode -> VM_LOGICAL_SENSOR
            R.id.rdo_device_node_mode -> VM_DEVICE_NODE
            else -> VM_PHYSICAL_SENSOR
        }
        sortDialog = null
        filterDialog = null
        searchDialog = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_data_browse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_data_source -> {
                //realTime = !realTime
                switchableFragmentManager.currentFragment.changeDataSource()
            }
            R.id.mi_sort -> {
                sortDialog = switchableFragmentManager.currentFragment.onSortButtonClick(sortDialog)
//                (sortDialog ?: switchableFragmentManager.currentFragment.onInitSortDialog(SortDialog()))
//                        .show(switchableFragmentManager.currentFragment.getChildFragmentManager(), "sort_dialog")
            }
            R.id.mi_filter -> {
                filterDialog = switchableFragmentManager.currentFragment.onFilterButtonClick(filterDialog)
//                (filterDialog ?: switchableFragmentManager.currentFragment.onInitFilterDialog(FilterDialog()))
//                        .show(switchableFragmentManager.currentFragment.getChildFragmentManager(), "filter_dialog")
            }
            R.id.mi_search -> {
                searchDialog = switchableFragmentManager.currentFragment.onSearchButtonClick(searchDialog)
//                (searchDialog ?: switchableFragmentManager.currentFragment.onInitSearchDialog(SearchDialog()))
//                        .show(switchableFragmentManager.currentFragment.getChildFragmentManager(), "search_dialog")
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private inner class ImportSensorConfigurationsTask : AsyncTask<Long, SensorManager.SensorConfigurationProvider, SensorManager.SensorConfigurationProvider?>() {

        override fun doInBackground(vararg params: Long?): SensorManager.SensorConfigurationProvider? {
            if (params == null || params.isEmpty()) {
                return null
            }
            val provider = SensorDatabase.importValueContainerConfigurationProvider(params[0]!!)
            SensorManager.setValueContainerConfigurationProvider(provider, true)
            return provider
        }

        override fun onPostExecute(provider: SensorManager.SensorConfigurationProvider?) {
            if (provider != null) {
                //创建CommonWarnProcessor
                //注，目前暂时硬编码，之后由设置界面进行修改
                //当前传感器列表界面和单一传感器数据界面使用同一个warnProcessor，
                //以后根据需求可以设置不同的warnProcessor
                warnProcessor = CommonWarnProcessor<View>()
                warnProcessor?.addExecutor(ViewBackgroundSingleRangeWarnExecutor(
                        this@DataBrowseActivity,
                        android.R.color.holo_red_light,
                        android.R.color.holo_green_light)
                )
                warnProcessor?.addExecutor(ViewBackgroundSwitchWarnExecutor(
                        this@DataBrowseActivity,
                        android.R.color.holo_red_light
                ))
                switchableFragmentManager.currentFragment.onImportWarnProcessor(warnProcessor!!)
            }
        }
    }
}
