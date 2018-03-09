package com.weisi.tool.wsnbox.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.*
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler
import com.cjq.tool.qbox.ui.adapter.HeaderAndFooterWrapper
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.EditDialog
import com.cjq.tool.qbox.ui.dialog.ListDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.cjq.tool.qbox.ui.loader.SimpleCursorLoader
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.ParameterConfigAdapter
import com.weisi.tool.wsnbox.io.Constant.*
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.permission.PermissionsRequester
import com.weisi.tool.wsnbox.permission.ReadPermissionsRequester
import com.weisi.tool.wsnbox.util.UriHelper
import kotlinx.android.synthetic.main.activity_parameter_configuration.*
import kotlinx.android.synthetic.main.list_item_para_config_insert.view.*
import kotlinx.android.synthetic.main.list_item_scene.view.*

class ParameterConfigurationActivity : BaseActivity(),
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        ListDialog.OnItemSelectedListener,
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener,
        BaseDialog.OnDialogConfirmListener,
        EditDialog.OnContentReceiver {

    private val REQUEST_CODE_FILE_SELECT = 1
    private val REQUEST_CODE_READ_PEMISSION = 2
    private val TOKEN_DELETE_CONFIG = 1
    private val TOKEN_UPDATE_CONFIG_NAME = 2
    private val DIALOG_TAG_CONFIRM_DELETE_CONFIG = "delete_config"
    private val DIALOG_TAG_EDIT_CONFIG_NAME = "edit_name"

    private val adapter = ParameterConfigAdapter()
    private val cvScenes = mutableListOf<View>()
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameter_configuration)

        rv_para_config.layoutManager = LinearLayoutManager(this)
        rv_para_config.addOnItemTouchListener(ConfigProviderTouchListener(rv_para_config)
                .addItemChildViewTouchEnabled(R.id.iv_export)
                .addItemChildViewTouchEnabled(R.id.iv_remove)
                .addItemChildViewTouchEnabled(R.id.iv_para_config_logo)
                .addItemChildViewTouchEnabled(R.id.tv_config_provider_name))

        var wrapper = HeaderAndFooterWrapper(adapter)
        var inflater = LayoutInflater.from(this);
        wrapper.addHeaderView(inflater.inflate(R.layout.list_item_para_config_description, rv_para_config, false))
        wrapper.addHeaderView(getGroupLabelView(inflater, R.string.config_list))
        wrapper.addFootView(getConfigInsertView(inflater))
        wrapper.addFootView(getGroupLabelView(inflater, R.string.scene_list))
        wrapper.addFootView(getSceneView(inflater, R.string.data_browse, R.drawable.ic_scene_data_browse))
        rv_para_config.adapter = wrapper

        supportLoaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return object : SimpleCursorLoader(this) {
            override fun loadInBackground(): Cursor? {
                return SensorDatabase.importValueContainerConfigurationProviders()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        adapter.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        adapter.swapCursor(data)
        updateScenesConfigName()
    }

    private fun updateScenesConfigName() {
        for (cvScene in cvScenes) {
            when (cvScene.tag) {
                R.string.data_browse -> {
                    updateSceneConfigName(cvScene, baseApplication.settings.dataBrowseValueContainerConfigurationProviderId)
                }
            }
        }
    }

    private fun updateSceneConfigName(vScene: View, id: Long) {
        vScene.tv_scene_config.text = adapter.findProviderNameById(id)
    }

    private fun getConfigInsertView(inflater: LayoutInflater): View {
        var view = inflater.inflate(R.layout.list_item_para_config_insert, rv_para_config, false)
        view.cv_add.setOnClickListener(this)
        view.cv_import.setOnClickListener(this)
        return view
    }

    private fun getGroupLabelView(inflater: LayoutInflater, @StringRes labelResId: Int): View {
        var view = inflater.inflate(R.layout.list_item_group_label, rv_para_config, false)
        if (view is TextView) {
            view.setText(labelResId)
        }
        return view
    }

    private fun getSceneView(inflater: LayoutInflater, @StringRes labelResId: Int, @DrawableRes logoRes: Int): View {
        var view = inflater.inflate(R.layout.list_item_scene, rv_para_config, false)
        view.tv_scene_label.setText(labelResId)
        view.iv_scene_logo.setImageResource(logoRes)
        view.setOnClickListener(this)
        view.tag = labelResId
        cvScenes.add(view)
        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cv_add -> {
                expectFunction()
            }
            R.id.cv_import -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ReadPermissionsRequester(this, REQUEST_CODE_READ_PEMISSION)
                            .requestPermissions(object : PermissionsRequester.OnRequestResultListener {
                                override fun onPermissionsGranted() {
                                    chooseConfigurationProvider()
                                }

                                override fun onPermissionsDenied() {
                                    SimpleCustomizeToast.show(this@ParameterConfigurationActivity, R.string.lack_read_permissions)
                                }
                            })
                } else {
                    chooseConfigurationProvider()
                }
            }
        }
        when (v?.tag) {
            R.string.data_browse -> {
                val configProviderNames = generateConfigProviderNames()
                if (configProviderNames !== null) {
                    val dialog = ListDialog()
                    dialog.setTitle(R.string.choose_scene_config)
                    dialog.setItems(configProviderNames)
                    dialog.arguments!!.putInt("scene_tag", v.tag as Int)
                    dialog.show(supportFragmentManager, "ld_choose_provider")
                }
            }
        }
    }

    private fun expectFunction() {
        val dialog = ConfirmDialog()
        dialog.setTitle(R.string.function_expect)
        dialog.setDrawCancelButton(false)
        dialog.show(supportFragmentManager,
                "function_expect")
    }

    private fun chooseConfigurationProvider() {
        var intent = Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(UriHelper.getUriByPath(this, baseApplication.settings.outputFilePath),
                "text/xml")
//        var file = File(baseApplication.settings.outputFilePath)
//        intent.setDataAndType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            FileProvider.getUriForFile(applicationContext,
//                    BuildConfig.APPLICATION_ID + ".provider",
//                    file)
//        } else {
//            Uri.fromFile(file)
//        }, "text/xml")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_config_provider)), REQUEST_CODE_FILE_SELECT)
        } catch (ex: android.content.ActivityNotFoundException) {
            SimpleCustomizeToast.show(this, R.string.file_manager_open_failed)
        }
    }

    private fun generateConfigProviderNames(): Array<String>? {
        var size = adapter.itemCount
        return if (size == 0) {
            null
        } else {
            Array(size + 1, { position ->
                if (position == size) {
                    getString(R.string.remove_config_provider)
                } else {
                    adapter.getProviderName(position)
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_FILE_SELECT) {
            //var filePath = data?.data?.path
            var filePath = UriHelper.getRealFilePath(this, data?.data)
            if (filePath.isNullOrEmpty()) {
                SimpleCustomizeToast.show(this, R.string.config_provider_null);
            } else {
                object : AsyncTask<Void, Void, Int>() {
                    override fun doInBackground(vararg params: Void): Int? {
//                        return if (params === null || params.isEmpty() || params[0] !is String) {
//                            0
//                        } else {
//                            SensorDatabase.insertValueContainerConfigurationProviderFromXml(this@ParameterConfigurationActivity, params[0])
//                        }
                        return SensorDatabase.insertValueContainerConfigurationProviderFromXml(filePath)
                    }

                    override fun onPostExecute(result: Int?) {
                        if (result!! > 0) {
                            adapter.scheduleItemRangeInsert(adapter.itemCount, result)
                            refreshConfigProviderList()
                        } else {
                            SimpleCustomizeToast.show(this@ParameterConfigurationActivity,
                                    R.string.insert_config_provider_failed)
                        }
                    }
                }.execute()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun rebindSceneAndProvider(providerId: Long) {
        if (baseApplication.settings.dataBrowseValueContainerConfigurationProviderId == providerId) {
            baseApplication.settings.dataBrowseValueContainerConfigurationProviderId = 0
        }
    }

    private fun refreshConfigProviderList() {
        supportLoaderManager.getLoader<Cursor>(0).onContentChanged()
    }

    override fun onItemSelected(dialog: ListDialog?, position: Int) {
        when (dialog!!.arguments!!.getInt("scene_tag")) {
            R.string.data_browse -> {
                baseApplication
                        .settings
                        .dataBrowseValueContainerConfigurationProviderId = if (position == adapter.itemCount) {
                    0
                } else {
                    adapter.getItemId(position)
                }
            }
        }
        //val vScene = findViewById<CardView>(dialog!!.arguments!!.getInt("scene_id"))
        //vScene.findViewById<TextView>(R.id.tv_scene_config).text = adapter.getProviderName(position)
        updateScenesConfigName()
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
    }

    override fun onDeleteComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_DELETE_CONFIG -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    rebindSceneAndProvider(adapter.getItemId(position))
                    adapter.scheduleItemRemove(position)
                    refreshConfigProviderList()
                } else {
                    SimpleCustomizeToast.show(this, R.string.delete_config_provider_failed)
                }
            }
        }
    }

    override fun onReplaceComplete(token: Int, cookie: Any?, rowId: Long) {
    }

    override fun onExecSqlComplete(token: Int, cookie: Any?, result: Boolean) {
    }

    override fun onInsertComplete(token: Int, cookie: Any?, rowId: Long) {
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_UPDATE_CONFIG_NAME -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    adapter.scheduleItemChange(position)
                    refreshConfigProviderList()
                } else {
                    SimpleCustomizeToast.show(this, R.string.modify_config_provider_name_failed)
                }
            }
        }
    }

    override fun onConfirm(dialog: BaseDialog<*>?): Boolean {
        when (dialog!!.tag) {
            DIALOG_TAG_CONFIRM_DELETE_CONFIG -> {
                val position = dialog.arguments!!.getInt("position")
                databaseHandler.startDelete(TOKEN_DELETE_CONFIG,
                        position, TABLE_CONFIGURATION_PROVIDER,
                        COLUMN_COMMON_ID + " = ?",
                        arrayOf(adapter.getItemId(position).toString()))
            }
        }
        return true
    }

    override fun onReceive(dialog: EditDialog?, oldValue: String?, newValue: String?): Boolean {
        when (dialog!!.tag) {
            DIALOG_TAG_EDIT_CONFIG_NAME -> {
                if (TextUtils.isEmpty(newValue)) {
                    val d = ConfirmDialog()
                    d.setTitle(R.string.config_provider_name_empty)
                    d.setDrawCancelButton(false)
                    d.show(supportFragmentManager, "ccne")
                    return false
                } else {
                    val position = dialog.arguments!!.getInt("position")
                    val values = ContentValues()
                    values.put(COLUMN_CONFIGURATION_PROVIDER_NAME, newValue)
                    databaseHandler.startUpdate(TOKEN_UPDATE_CONFIG_NAME,
                            position, TABLE_CONFIGURATION_PROVIDER,
                            values, COLUMN_COMMON_ID + " = ?",
                            arrayOf(adapter.getItemId(position).toString()),
                            CONFLICT_NONE)
                }
            }
        }
        return true
    }

    private inner class ConfigProviderTouchListener(rv: RecyclerView) : SimpleRecyclerViewItemTouchListener(rv) {

        override fun onItemClick(v: View?, position: Int) {
            when (v!!.id) {
                R.id.iv_remove -> {
                    val dialog = ConfirmDialog()
                    dialog.setTitle(R.string.confirm_delete_config_provider)
                    dialog.arguments!!.putInt("position", position)
                    dialog.show(supportFragmentManager, DIALOG_TAG_CONFIRM_DELETE_CONFIG)
                }
                R.id.iv_para_config_logo -> {
                    val dialog = EditDialog()
                    dialog.setTitle(R.string.modify_config_provider_name)
                    dialog.setContent(adapter.getProviderName(position))
                    dialog.arguments!!.putInt("position", position)
                    dialog.show(supportFragmentManager, DIALOG_TAG_EDIT_CONFIG_NAME)
                }
                R.id.iv_export, R.id.tv_config_provider_name -> {
                    expectFunction()
                }
            }
        }
    }
}
