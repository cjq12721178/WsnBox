package com.weisi.tool.wsnbox.fragment.config

import com.weisi.tool.wsnbox.fragment.BaseFragment
import com.weisi.tool.wsnbox.io.Constant

open class ConfigurationFragment : BaseFragment() {

    protected fun getConfigurationProviderId() =
            activity?.intent?.getLongExtra(Constant.COLUMN_CONFIGURATION_PROVIDER_ID, -1) ?: -1

}