package com.weisi.tool.wsnbox.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import com.weisi.tool.wsnbox.activity.BaseActivity
import com.weisi.tool.wsnbox.service.DataPrepareService

open class BaseFragment2 : Fragment(), BaseFragmentFunction {

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
    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
    }

    override fun invalid(): Boolean {
        return functionDelegate.isFragmentInvalid()
    }
}