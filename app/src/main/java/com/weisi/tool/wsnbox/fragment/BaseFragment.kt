package com.weisi.tool.wsnbox.fragment

import android.support.v4.app.Fragment
import com.weisi.tool.wsnbox.activity.BaseActivity
import com.weisi.tool.wsnbox.service.DataPrepareService

/**
 * Created by CJQ on 2018/5/24.
 */
open class BaseFragment : Fragment() {

    open fun getBaseActivity() : BaseActivity {
        return activity as BaseActivity;
    }

    open fun onServiceConnectionCreate(service: DataPrepareService) {
    }

    open fun onServiceConnectionStart(service: DataPrepareService) {
    }

    open fun onServiceConnectionStop(service: DataPrepareService) {
    }

    open fun onServiceConnectionDestroy(service: DataPrepareService) {
    }
}