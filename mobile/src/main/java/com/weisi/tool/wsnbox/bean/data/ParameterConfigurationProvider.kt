package com.weisi.tool.wsnbox.bean.data

class ParameterConfigurationProvider(val type: Int, val devices: List<Device>) {

    companion object {
        const val TYPE_COMMON = 0
        const val TYPE_INTELLIGENT_GASKET = 1
    }
}