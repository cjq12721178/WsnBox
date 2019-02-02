package com.weisi.tool.wsnbox.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
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
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler
import com.cjq.tool.qbox.ui.adapter.HeaderAndFooterWrapper
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.EditDialog
import com.cjq.tool.qbox.ui.dialog.ListDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.config.ParameterConfigAdapter
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.Constant.*
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.permission.PermissionsRequester
import com.weisi.tool.wsnbox.permission.ReadPermissionsRequester
import com.weisi.tool.wsnbox.processor.importer.ConfigurationProviderXmlImporter
import com.weisi.tool.wsnbox.processor.loader.ConfigurationProvidersInfoLoader
import com.weisi.tool.wsnbox.util.SafeAsyncTask
import com.weisi.tool.wsnbox.util.UriHelper
import kotlinx.android.synthetic.main.activity_parameter_configuration.*
import kotlinx.android.synthetic.main.li_para_config_insert.view.*
import kotlinx.android.synthetic.main.li_scene.view.*

class ParameterConfigurationActivity : BaseActivity(),
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        ListDialog.OnItemSelectedListener,
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener,
        BaseDialog.OnDialogConfirmListener,
        EditDialog.OnContentReceiver, SafeAsyncTask.ResultAchiever<Int, Void> {

    private val REQUEST_CODE_FILE_SELECT = 2
    private val REQUEST_CODE_READ_PEMISSION = 3
    private val TOKEN_DELETE_CONFIG = 1
    private val TOKEN_UPDATE_CONFIG_NAME = 2
    private val TOKEN_INSERT_CONFIG = 3
    private val ARGUMENT_KEY_NEW_CONFIG_POSITION = "new_cfg_pos"
    private val DIALOG_TAG_CONFIRM_DELETE_CONFIG = "delete_config"
    private val DIALOG_TAG_EDIT_CONFIG_NAME = "edit_name"
    private val DIALOG_TAG_EDIT_ADD_CONFIG = "et_add"
    private val DIALOG_TAG_EXPORT_CONFIG = "exp_cfg"
    private val ARGUEMENT_KEY_SCENE_TYPE = "scene_type"

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

        val wrapper = HeaderAndFooterWrapper(adapter)
        val inflater = LayoutInflater.from(this)
        wrapper.addHeaderView(inflater.inflate(R.layout.lh_para_config_description, rv_para_config, false))
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_para_config, R.string.config_list))
        wrapper.addFootView(getConfigInsertView(inflater))
        wrapper.addFootView(getGroupLabelView(inflater, rv_para_config, R.string.scene_list))
        wrapper.addFootView(getSceneView(inflater, R.string.data_browse, R.drawable.ic_scene_data_browse))
        wrapper.addFootView(getSceneView(inflater, R.string.product_display, R.drawable.ic_scene_product_display))
        rv_para_config.adapter = wrapper

        supportLoaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return ConfigurationProvidersInfoLoader(this)
//        return object : SimpleCursorLoader(this) {
//            override fun loadInBackground(): Cursor? {
//                return SensorDatabase.importValueContainerConfigurationProviders()
//            }
//        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        adapter.swapCursor(data)
        updateScenesConfigName()
        startNewParameterConfigurationActivityIfPossible()
    }

    private fun updateScenesConfigName() {
        for (cvScene in cvScenes) {
            when (cvScene.tag) {
                R.string.data_browse -> {
                    updateSceneConfigName(cvScene, baseApplication.settings.dataBrowseValueContainerConfigurationProviderId)
                }
                R.string.product_display -> {
                    updateSceneConfigName(cvScene, baseApplication.settings.productDisplayValueContainerConfigurationProviderId)
                }
            }
        }
    }

    private fun startNewParameterConfigurationActivityIfPossible() {
        val position = intent.getIntExtra(ARGUMENT_KEY_NEW_CONFIG_POSITION, -1)
        if (position != -1) {
            intent.putExtra(ARGUMENT_KEY_NEW_CONFIG_POSITION, -1)
            startProviderConfigurationActivity(position)
        }
    }

    private fun startProviderConfigurationActivity(position: Int) {
        val intent = Intent(this, ProviderConfigurationActivity::class.java)
        intent.putExtra(Constant.COLUMN_CONFIGURATION_PROVIDER_NAME, adapter.getProviderName(position))
        intent.putExtra(Constant.COLUMN_CONFIGURATION_PROVIDER_ID, adapter.getItemId(position))
        startActivity(intent)
    }

    private fun updateSceneConfigName(vScene: View, id: Long) {
        vScene.tv_scene_config.text = adapter.findProviderNameById(id)
    }

    private fun getConfigInsertView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.li_para_config_insert, rv_para_config, false)
        view.cv_add.setOnClickListener(this)
        view.cv_import.setOnClickListener(this)
        return view
    }

//    private fun getGroupLabelView(inflater: LayoutInflater, @StringRes labelResId: Int): View {
//        var view = inflater.inflate(R.layout.li_group_label, rv_para_config, false)
//        if (view is TextView) {
//            view.setText(labelResId)
//        }
//        return view
//    }

    private fun getSceneView(inflater: LayoutInflater, @StringRes labelResId: Int, @DrawableRes logoRes: Int): View {
        val view = inflater.inflate(R.layout.li_scene, rv_para_config, false)
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
                val dialog = EditDialog()
                dialog.setTitle(R.string.input_parameter_config_name)
                dialog.setContent(R.string.sensor_config)
                dialog.show(supportFragmentManager, DIALOG_TAG_EDIT_ADD_CONFIG)
            }
            R.id.cv_import -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ReadPermissionsRequester(this, REQUEST_CODE_READ_PEMISSION)
                            .requestPermissions(object : PermissionsRequester.OnRequestResultListener {
                                override fun onPermissionsGranted() {
                                    chooseConfigurationProvider()
                                }

                                override fun onPermissionsDenied() {
                                    SimpleCustomizeToast.show(R.string.lack_read_permissions)
                                }
                            })
                } else {
                    chooseConfigurationProvider()
                }
            }
        }
        when (v?.tag) {
            R.string.data_browse, R.string.product_display -> {
                generateConfigProviderNames()?.let {
                    val dialog = ListDialog()
                    dialog.setTitle(R.string.choose_scene_config)
                    dialog.setItems(it)
                    dialog.arguments!!.putInt(ARGUEMENT_KEY_SCENE_TYPE, v.tag as Int)
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
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setDataAndType(UriHelper.getUriByPath(this, baseApplication.settings.outputFilePath),
                "text/xml")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_config_provider)), REQUEST_CODE_FILE_SELECT)
        } catch (ex: android.content.ActivityNotFoundException) {
            SimpleCustomizeToast.show(R.string.file_manager_open_failed)
        }
    }

    private fun generateConfigProviderNames(): Array<String>? {
        val size = adapter.itemCount
        return if (size == 0) {
            null
        } else {
            Array(size + 1) { position ->
                if (position == size) {
                    getString(R.string.remove_config_provider)
                } else {
                    adapter.getProviderName(position)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_FILE_SELECT) {
            //var filePath = data?.data?.path
            val filePath = UriHelper.getRealFilePath(this, data?.data)
            if (filePath.isNullOrEmpty()) {
                SimpleCustomizeToast.show(R.string.config_provider_null)
            } else {
                ConfigurationProviderXmlImporter(this).execute(filePath)
//                object : AsyncTask<Void, Void, Int>() {
//                    override fun doInBackground(vararg params: Void): Int? {
////                        return if (params === null || params.isEmpty() || params[0] !is String) {
////                            0
////                        } else {
////                            SensorDatabase.insertValueContainerConfigurationProviderFromXml(this@ParameterConfigurationActivity, params[0])
////                        }
//                        return SensorDatabase.insertValueContainerConfigurationProviderFromXml(filePath)
//                    }
//
//                    override fun onPostExecute(result: Int?) {
//                        if (result!! > 0) {
//                            adapter.scheduleItemRangeInsert(adapter.itemCount, result)
//                            refreshConfigProviderList()
//                        } else {
//                            SimpleCustomizeToast.show(R.string.insert_config_provider_failed)
//                        }
//                    }
//                }.execute()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResultAchieved(result: Int?) {
        val providerId = result ?: -1
        if (providerId > 0) {
            adapter.scheduleItemRangeInsert(adapter.itemCount, providerId)
            refreshConfigProviderList()
        } else {
            SimpleCustomizeToast.show(R.string.insert_config_provider_failed)
        }
    }

    private fun rebindSceneAndProvider(providerId: Long) {
        if (baseApplication.settings.dataBrowseValueContainerConfigurationProviderId == providerId) {
            baseApplication.settings.dataBrowseValueContainerConfigurationProviderId = 0
        }
        if (baseApplication.settings.productDisplayValueContainerConfigurationProviderId == providerId) {
            baseApplication.settings.productDisplayValueContainerConfigurationProviderId = 0
        }
    }

    private fun refreshConfigProviderList() {
        supportLoaderManager.getLoader<Cursor>(0)?.onContentChanged()
    }

    override fun onItemSelected(dialog: ListDialog, position: Int, items: Array<out Any>) {
        val selectedProviderId = if (position == adapter.itemCount) {
            0
        } else {
            adapter.getItemId(position)
        }
        when (dialog.arguments!!.getInt(ARGUEMENT_KEY_SCENE_TYPE)) {
            R.string.data_browse -> {
                baseApplication
                        .settings
                        .dataBrowseValueContainerConfigurationProviderId = selectedProviderId
            }
            R.string.product_display -> {
                baseApplication
                        .settings
                        .productDisplayValueContainerConfigurationProviderId = selectedProviderId
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
                    SimpleCustomizeToast.show(R.string.delete_config_provider_failed)
                }
            }
        }
    }

    override fun onReplaceComplete(token: Int, cookie: Any?, rowId: Long) {
    }

    override fun onExecSqlComplete(token: Int, cookie: Any?, result: Boolean) {
    }

    override fun onInsertComplete(token: Int, cookie: Any?, rowId: Long) {
        when (token) {
            TOKEN_INSERT_CONFIG -> {
                if (rowId != -1L) {
                    val size = cookie as Int
                    adapter.scheduleItemInsert(size)
                    intent.putExtra(ARGUMENT_KEY_NEW_CONFIG_POSITION, size)
                    refreshConfigProviderList()
                } else {
                    SimpleCustomizeToast.show(R.string.add_config_provider_failed)
                }
            }
        }
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_UPDATE_CONFIG_NAME -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    adapter.scheduleItemChange(position)
                    intent.putExtra(ARGUMENT_KEY_NEW_CONFIG_POSITION, position)
                    refreshConfigProviderList()
                } else {
                    SimpleCustomizeToast.show(R.string.modify_config_provider_name_failed)
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
                        "$COLUMN_COMMON_ID = ?",
                        arrayOf(adapter.getItemId(position).toString()))
            }
            DIALOG_TAG_EXPORT_CONFIG -> {
                expectFunction()
//                object : AsyncTask<Long, Boolean, Boolean>() {
//                    override fun doInBackground(vararg params: Long?): Boolean {
//                        val providerId = params[0] ?: return false
//                        val provider = SensorDatabase.importMeasurementConfigurationProvider(providerId) ?: return false
//                        val devices = mutableListOf<Device>()
//                        return if (SensorDatabase.importDevices(providerId) {
//                            devices.add(it)
//                        }) {
//
//                            return true
//                        } else {
//                            false
//                        }
//                    }
//                }.execute(dialog.arguments?.getLong(COLUMN_COMMON_ID))
            }
        }
        return true
    }

    override fun onReceive(dialog: EditDialog?, oldValue: String?, newValue: String?): Boolean {
        when (dialog!!.tag) {
            DIALOG_TAG_EDIT_CONFIG_NAME -> {
                if (!checkConfigName(newValue)) {
                    return false
                } else {
                    val position = dialog.arguments!!.getInt("position")
                    val values = ContentValues()
                    values.put(COLUMN_CONFIGURATION_PROVIDER_NAME, newValue)
                    values.put(COLUMN_MODIFY_TIME, System.currentTimeMillis())
                    databaseHandler.startUpdate(TOKEN_UPDATE_CONFIG_NAME,
                            position, TABLE_CONFIGURATION_PROVIDER,
                            values, "$COLUMN_COMMON_ID = ?",
                            arrayOf(adapter.getItemId(position).toString()),
                            CONFLICT_NONE)
                }
            }
            DIALOG_TAG_EDIT_ADD_CONFIG -> {
                if (!checkConfigName(newValue)) {
                    return false
                } else {
                    val values = ContentValues()
                    values.put(COLUMN_CONFIGURATION_PROVIDER_NAME, newValue)
                    values.put(COLUMN_CREATE_TIME, System.currentTimeMillis())
                    values.put(COLUMN_MODIFY_TIME, System.currentTimeMillis())
                    databaseHandler.startInsert(TOKEN_INSERT_CONFIG,
                            adapter.itemCount,
                            TABLE_CONFIGURATION_PROVIDER,
                            values,
                            CONFLICT_NONE)
                }
            }
        }
        return true
    }

    private fun checkConfigName(name: String?) : Boolean {
        return if (TextUtils.isEmpty(name)) {
            val d = ConfirmDialog()
            d.setTitle(R.string.config_provider_name_empty)
            d.setDrawCancelButton(false)
            d.show(supportFragmentManager, "ccne")
            false
        } else {
            true
        }
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
                R.id.iv_export -> {
                    val dialog = ConfirmDialog()
                    dialog.setTitle(R.string.export_configuration_provider)
                    dialog.arguments?.putLong(COLUMN_COMMON_ID, adapter.getItemId(position))
                    dialog.show(supportFragmentManager, DIALOG_TAG_EXPORT_CONFIG)
                }
                R.id.tv_config_provider_name -> {
                    startProviderConfigurationActivity(position)
                }
            }
        }
    }
}
