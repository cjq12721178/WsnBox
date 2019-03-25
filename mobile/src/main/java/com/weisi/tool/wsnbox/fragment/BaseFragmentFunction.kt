package com.weisi.tool.wsnbox.fragment

import com.weisi.tool.wsnbox.activity.BaseActivity
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener
import com.weisi.tool.wsnbox.util.SafeAsyncTask

interface BaseFragmentFunction : OnServiceConnectionListener, SafeAsyncTask.AchieverChecker {
    fun getBaseActivity(): BaseActivity?
    fun performServiceConnectionCreate()
    fun performServiceConnectionStart()

//    companion object {
//        const val ARGUMENT_KEY_REGISTER_TAG = "reg_fragment"
//    }
}