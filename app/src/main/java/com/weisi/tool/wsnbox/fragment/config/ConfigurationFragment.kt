package com.weisi.tool.wsnbox.fragment.config

import com.cjq.tool.qbox.ui.manager.OnDataSetChangedListener
import com.weisi.tool.wsnbox.fragment.BaseFragment
import com.weisi.tool.wsnbox.io.Constant

open class ConfigurationFragment : BaseFragment(), OnDataSetChangedListener {

    protected fun getConfigurationProviderId() =
            activity?.intent?.getLongExtra(Constant.COLUMN_CONFIGURATION_PROVIDER_ID, -1) ?: -1

    override fun onDataSetChanged() {

    }

    open fun onAdd() {
    }

    open fun onDelete() {
    }
}