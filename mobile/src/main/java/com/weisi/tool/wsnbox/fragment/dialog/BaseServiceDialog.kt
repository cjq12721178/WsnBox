package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.weisi.tool.wsnbox.activity.BaseActivity
import com.weisi.tool.wsnbox.fragment.BaseFragmentFunction
import com.weisi.tool.wsnbox.fragment.FragmentFunctionDelegate
import com.weisi.tool.wsnbox.service.DataPrepareService

/**
 * Created by CJQ on 2018/4/3.
 */
open class BaseServiceDialog : DialogFragment(), BaseFragmentFunction {

    private val functionDelegate = FragmentFunctionDelegate(this)

    override fun getBaseActivity(): BaseActivity? = functionDelegate.getBaseActivity()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        functionDelegate.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        functionDelegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        functionDelegate.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        functionDelegate.onDestroy()
    }

    override fun performServiceConnectionCreate() {
        functionDelegate.performServiceConnectionCreate()
    }

    override fun performServiceConnectionStart() {
        functionDelegate.performServiceConnectionStart()
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "dialog onServiceConnectionCreate")
    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "dialog onServiceConnectionStart")
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "dialog onServiceConnectionStop")
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "dialog onServiceConnectionDestroy")
    }

    override fun invalid(): Boolean {
        return functionDelegate.isFragmentInvalid()
    }
}