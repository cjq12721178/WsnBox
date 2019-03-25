package com.weisi.tool.wsnbox.bean.decorator

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonMeasurementDecorator(customName: String, val customUnit: String, savedDecimals: Int) : CommonBaseDecorator(customName) {

    private val decimals: Int = if (savedDecimals < 0 || savedDecimals > 9) {
        -1
    } else {
        savedDecimals
    }

    override fun decorateValue(rawValue: Double, para: Int): String {
        return if (customUnit.isEmpty()) {
            ""
        } else {
            String.format("%.${getRefinedDecimals()}f$customUnit", rawValue)
        }
    }

    fun getOriginDecimals() = decimals

    fun getOriginDecimalsLabel(): String {
        return if (decimals == -1) {
            ""
        } else {
            decimals.toString()
        }
    }

    fun getRefinedDecimals() = if (decimals == -1) {
        3
    } else {
        decimals
    }
}