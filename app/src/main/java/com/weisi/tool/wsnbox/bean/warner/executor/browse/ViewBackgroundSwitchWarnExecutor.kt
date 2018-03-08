package com.weisi.tool.wsnbox.bean.warner.executor.browse

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import com.weisi.tool.wsnbox.bean.warner.executor.SwitchWarnExecutor

/**
 * Created by CJQ on 2018/2/9.
 */
class ViewBackgroundSwitchWarnExecutor : ViewBackgroundWarnExecutor, SwitchWarnExecutor<View> {

    private val abnormalColor: Int

    constructor(context: Context,
                @ColorRes abnormalColorRes: Int) : super(context) {
        abnormalColor = ContextCompat.getColor(context, abnormalColorRes)
    }

    override fun onResultInAbnormalState(env: View) {
        env.setBackgroundColor(abnormalColor)
    }
}