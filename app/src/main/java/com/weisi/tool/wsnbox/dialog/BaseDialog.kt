package com.weisi.tool.wsnbox.dialog

import android.support.v4.app.DialogFragment
import com.weisi.tool.wsnbox.activity.BaseActivity

/**
 * Created by CJQ on 2018/4/3.
 */
open class BaseDialog : DialogFragment() {

    fun getBaseActivity() : BaseActivity? {
        return activity as BaseActivity?;
    }
}