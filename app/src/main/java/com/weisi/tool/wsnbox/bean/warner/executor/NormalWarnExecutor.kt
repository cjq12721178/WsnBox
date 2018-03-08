package com.weisi.tool.wsnbox.bean.warner.executor

/**
 * Created by CJQ on 2018/2/9.
 */
interface NormalWarnExecutor<E> {
    fun onResultNormal(env: E)
}