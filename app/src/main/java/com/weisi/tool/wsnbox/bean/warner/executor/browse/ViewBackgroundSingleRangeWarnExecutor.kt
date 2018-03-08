package com.weisi.tool.wsnbox.bean.warner.executor.browse

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import com.weisi.tool.wsnbox.bean.warner.executor.SingleRangeWarnExecutor

/**
 * Created by CJQ on 2018/2/9.
 */
class ViewBackgroundSingleRangeWarnExecutor : ViewBackgroundWarnExecutor, SingleRangeWarnExecutor<View> {

    private val aboveColor: Int
    private val belowColor: Int

    constructor(context: Context,
                @ColorRes aboveColorRes: Int,
                @ColorRes belowColorRes: Int) : super(context) {
        aboveColor = ContextCompat.getColor(context, aboveColorRes)
        belowColor = ContextCompat.getColor(context, belowColorRes)
    }

    override fun onResultAboveHighLimit(env: View) {
        env.setBackgroundColor(aboveColor)
    }

    override fun onResultBelowLowLimit(env: View) {
        env.setBackgroundColor(belowColor)
    }
}