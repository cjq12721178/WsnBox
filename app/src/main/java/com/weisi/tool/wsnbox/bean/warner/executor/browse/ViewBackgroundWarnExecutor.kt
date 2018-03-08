package com.weisi.tool.wsnbox.bean.warner.executor.browse

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.weisi.tool.wsnbox.bean.warner.executor.BackgroundNormalWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.executor.NormalWarnExecutor

/**
 * Created by CJQ on 2018/2/9.
 */
abstract class ViewBackgroundWarnExecutor : BackgroundNormalWarnExecutor<View> {

    private val normalColor: Int

    constructor(context: Context) {
        normalColor = ContextCompat.getColor(context, android.R.color.transparent)
    }

    override fun onResultNormal(env: View) {
        env.setBackgroundColor(normalColor);
    }
}