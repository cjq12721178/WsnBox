package com.weisi.tool.wsnbox.bean.warner.executor

/**
 * Created by CJQ on 2018/2/9.
 */
interface SingleRangeWarnExecutor<E> : NormalWarnExecutor<E> {
    fun onResultAboveHighLimit(env: E)
    fun onResultBelowLowLimit(env: E)
}