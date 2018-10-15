package com.weisi.tool.wsnbox.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.RadioGroup
import com.cjq.tool.qbox.ui.manager.SwitchableFragmentManager
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.fragment.config.ConfigurationFragment
import com.weisi.tool.wsnbox.fragment.config.DeviceConfigurationFragment
import com.weisi.tool.wsnbox.fragment.config.SensorConfigurationFragment
import com.weisi.tool.wsnbox.io.Constant
import kotlinx.android.synthetic.main.activity_provider_configuration.*

class ProviderConfigurationActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private lateinit var switchableFragmentManager: SwitchableFragmentManager<ConfigurationFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_configuration)

        //标题
        title = intent.getStringExtra(Constant.COLUMN_CONFIGURATION_PROVIDER_NAME)

        //事件
        btn_add.setOnClickListener(this)
        btn_delete.setOnClickListener(this)

        //Tab
        switchableFragmentManager = SwitchableFragmentManager(supportFragmentManager,
                R.id.fl_view_carrier,
                arrayOf(getString(R.string.physical_sensor_mode),
                        getString(R.string.device_node_mode)),
                arrayOf(SensorConfigurationFragment::class.java,
                        DeviceConfigurationFragment::class.java))
        rg_config_mode.setOnCheckedChangeListener(this)
        rdo_sensors.isChecked = true
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        switchableFragmentManager.switchTo(when (checkedId) {
            R.id.rdo_sensors -> getString(R.string.physical_sensor_mode)
            R.id.rdo_devices -> getString(R.string.device_node_mode)
            else -> throw IllegalArgumentException("change configuration mode failed")
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add -> switchableFragmentManager.currentFragment.onAdd()
            R.id.btn_delete -> switchableFragmentManager.currentFragment.onDelete()
        }
    }
}
