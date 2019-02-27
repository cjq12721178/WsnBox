package com.weisi.tool.wsnbox.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.weisi.tool.wsnbox.BuildConfig
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.group_has_new_version.*


class AboutActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        tv_app_name_version_code.text = if (BuildConfig.DEBUG) {
            getString(R.string.app_name_debug)
        } else {
            getString(R.string.app_name)
        } + BuildConfig.VERSION_NAME

        if (getBaseApplication().settings.latestVersionName != BuildConfig.VERSION_NAME) {
            vs_has_new_version.inflate()
            tv_new_version.text = getBaseApplication().settings.latestVersionName
        } else {
            tv_version_update.setText(R.string.check_latest_version)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_function_introduction, R.id.tv_prompt_error -> {
                showExpectDialog()
            }
            R.id.ll_version_update -> {
                checkVersionAndDecideIfUpdate(true)
            }
            R.id.tv_contact_us -> {
                startActivity(Intent("android.intent.action.VIEW",
                        Uri.parse(getString(R.string.official_website))))
            }
        }
    }

    override fun onConfirm(dialog: BaseDialog<*>): Boolean {
        if (super.onConfirm(dialog)) {
            return true
        }
        when (dialog.tag) {
            DIALOG_TAG_UPDATE_APP -> {
                updateVersion(dialog)
            }
        }
        return true
    }
}
