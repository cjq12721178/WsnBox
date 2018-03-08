package com.weisi.tool.wsnbox.bean.warner.executor

/**
 * Created by CJQ on 2018/2/9.
 */
interface SwitchWarnExecutor<E> : NormalWarnExecutor<E> {
    fun onResultInAbnormalState(env: E)
}