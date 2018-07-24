package com.weisi.tool.wsnbox.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.weisi.tool.wsnbox.BuildConfig
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        tv_app_name_version_code.text = if (BuildConfig.DEBUG) {
            getString(R.string.app_name_debug)
        } else {
            getString(R.string.app_name)
        } + BuildConfig.VERSION_NAME
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_function_introduction -> {

            }
            R.id.tv_prompt_error -> {

            }
            R.id.tv_version_update -> {

            }
            R.id.tv_contact_us -> {
                startActivity(Intent("android.intent.action.VIEW",
                        Uri.parse("http://www.wsn-cn.com")))
            }
        }
    }
}
