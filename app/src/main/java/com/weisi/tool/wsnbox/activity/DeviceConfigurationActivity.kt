package com.weisi.tool.wsnbox.activity

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.SensorManager
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
import com.weisi.tool.wsnbox.adapter.config.DeviceConfigAdapter
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import kotlinx.android.synthetic.main.activity_device_configuration.*
import kotlinx.android.synthetic.main.lh_device_base_config.view.*

class DeviceConfigurationActivity : BaseActivity(),
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener,
        BaseDialog.OnDialogConfirmListener,
        BaseDialog.OnDialogCancelListener,
        EditDialog.OnContentReceiver,
        ListDialog.OnMultipleItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private val DIALOG_TAG_MODIFY_DEVICE_NAME = "mdf_dev_name"
    private val DIALOG_TAG_CONFIRM_DELETE_NODE_CONFIG = "confirm_del_node_cfg"
    private val DIALOG_TAG_MODIFY_NODE_NAME = "mdf_node_name"
    private val DIALOG_TAG_INSERT_NODES = "ins_nodes"
    private val TOKEN_UPDATE_DEVICE_NAME = 1
    private val TOKEN_DELETE_NODE_CONFIG = 2
    private val TOKEN_UPDATE_NODE_NAME = 3
    private val TOKEN_INSERT_NODE_CONFIG = 4
    private val ARGUMENT_KEY_NODE_POSITION = "node_pos"

    private val adapter = DeviceConfigAdapter()
    private lateinit var vBaseInfo: View
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_configuration)

        btn_add.setOnClickListener() {
            val dialog = EditDialog()
            dialog.setTitle(R.string.input_sensor_address_or_measurement_id)
            dialog.setSummary(R.string.insert_node_input_description)
            dialog.customDecorator.contentGroupWidth = ConstraintSet.WRAP_CONTENT
            dialog.show(supportFragmentManager, DIALOG_TAG_INSERT_NODES)
        }
        btn_select.setOnClickListener() {
            val addresses = getConfiguredSensorAddresses()
            if (addresses?.isNotEmpty() == true) {
                val dialog = ListDialog()
                dialog.setTitle(R.string.select_configured_sensors)
                dialog.setItems(addresses)
                dialog.isMultipleSelect = true
                dialog.show(supportFragmentManager, DIALOG_TAG_INSERT_NODES)
            } else {
                btn_add.performClick()
            }
        }
        btn_delete.setOnClickListener() {
            if (adapter.inDeleteMode) {
                val dialog = ConfirmDialog()
                dialog.setTitle(R.string.confirm_delete_node_config)
                dialog.show(supportFragmentManager, DIALOG_TAG_CONFIRM_DELETE_NODE_CONFIG)
            } else {
                adapter.changeDeleteModeWithNotification()
            }
        }

        rv_device_config.layoutManager = LinearLayoutManager(this)
        rv_device_config.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(rv_device_config) {
            override fun onItemClick(v: View?, position: Int) {
                if (adapter.inDeleteMode) {
                    adapter.selectDeletingItem(position)
                    adapter.notifyItemChanged(position)
                } else {
                    when (v?.id) {
                        R.id.tv_node_name_value -> {
                            val dialog = EditDialog()
                            dialog.setTitle(R.string.modify_node_name)
                            dialog.setContent(adapter.getNodeName(position))
                            dialog.setSummary(R.string.use_measurement_default_name_while_node_name_empty)
                            dialog.customDecorator.contentGroupWidth = ConstraintSet.WRAP_CONTENT
                            dialog.arguments?.putInt(ARGUMENT_KEY_NODE_POSITION, position)
                            dialog.arguments?.putLong(Constant.COLUMN_COMMON_ID, adapter.getItemId(position))
                            dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_NODE_NAME)
                        }
                    }
                }
            }
        }.addItemChildViewTouchEnabled(R.id.tv_node_name_value))

        val wrapper = HeaderAndFooterWrapper(adapter)
        val inflater = LayoutInflater.from(this)
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_device_config, R.string.basic_info))
        wrapper.addHeaderView(getBaseInfoView(inflater))
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_device_config, R.string.node_list))
        rv_device_config.adapter = wrapper

        supportLoaderManager.initLoader(0, null, this)
    }

    private fun getConfiguredSensorAddresses() = intent.getStringArrayExtra(Constant.TAG_ADDRESSES)

    private fun getBaseInfoView(inflater: LayoutInflater): View {
        vBaseInfo = inflater.inflate(R.layout.lh_device_base_config, rv_device_config, false)
        vBaseInfo.tv_device_name.setOnClickListener() {
            val dialog = EditDialog()
            dialog.setTitle(R.string.modify_device_name)
            dialog.setContent(getDeviceName())
            dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_DEVICE_NAME)
        }
        return vBaseInfo
    }

    private fun getDeviceName() =
            intent.getStringExtra(Constant.COLUMN_DEVICE_NAME)

    private fun getDeviceId() =
            intent.getLongExtra(Constant.COLUMN_COMMON_ID, -1)

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return object : SimpleCursorLoader(this) {
            override fun loadInBackground(): Cursor? {
                return SensorDatabase.importNodes(getDeviceId())
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        updateBaseInfo()
        adapter.swapCursor(data)
    }

    private fun updateBaseInfo() {
        vBaseInfo.tv_device_name.text = getDeviceName()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        adapter.changeCursor(null)
    }

    override fun onConfirm(dialog: BaseDialog<*>?): Boolean {
        when (dialog?.tag) {
            DIALOG_TAG_CONFIRM_DELETE_NODE_CONFIG -> {
                adapter.getDeletingItemsPosition().forEach() {
                    databaseHandler.startDelete(TOKEN_DELETE_NODE_CONFIG,
                            it, Constant.TABLE_NODE,
                            "${Constant.COLUMN_COMMON_ID} = ?",
                            arrayOf(adapter.getItemId(it).toString()))
                }
                adapter.inDeleteMode = !adapter.inDeleteMode
            }
        }
        return true
    }

    override fun onCancel(dialog: BaseDialog<*>?) {
        when (dialog?.tag) {
            DIALOG_TAG_CONFIRM_DELETE_NODE_CONFIG -> {
                adapter.changeDeleteModeWithNotification()
            }
        }
    }

    override fun onReceive(dialog: EditDialog?, oldValue: String?, newValue: String?): Boolean {
        if (TextUtils.equals(oldValue, newValue)) {
            return true
        }
        when (dialog?.tag) {
            DIALOG_TAG_MODIFY_DEVICE_NAME -> {
                if (newValue.isNullOrEmpty() || newValue.isNullOrBlank()) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.device_name_empty)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, "confirm_dev_name_empty")
                    return false
                }
                if (intent.getStringArrayExtra(Constant.TAG_NAMES).binarySearch(newValue) >= 0) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.device_name_duplicated)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, "dev_name_duplicate")
                    return false
                }
                val values = ContentValues()
                values.put(Constant.COLUMN_DEVICE_NAME, newValue)
                databaseHandler.startUpdate(TOKEN_UPDATE_DEVICE_NAME,
                        newValue, Constant.TABLE_DEVICE,
                        values, "${Constant.COLUMN_COMMON_ID} = ?",
                        arrayOf(getDeviceId().toString()),
                        SQLiteDatabase.CONFLICT_NONE)
            }
            DIALOG_TAG_MODIFY_NODE_NAME -> {
                val values = ContentValues()
                values.put(Constant.COLUMN_NODE_NAME, newValue)
                databaseHandler.startUpdate(TOKEN_UPDATE_NODE_NAME,
                        dialog.arguments?.getInt(ARGUMENT_KEY_NODE_POSITION),
                        Constant.TABLE_NODE, values,
                        "${Constant.COLUMN_COMMON_ID} = ?",
                        arrayOf(dialog.arguments?.getLong(Constant.COLUMN_COMMON_ID).toString()),
                        SQLiteDatabase.CONFLICT_NONE)
            }
            DIALOG_TAG_INSERT_NODES -> {
                val id = ID.parse(newValue)
                if (id == ID.INVALID_ID) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.sensor_or_measurement_id_parse_failed)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, "addr_id_err")
                    return false
                }
                val deviceId = getDeviceId()
                if (ID.isPhysicalSensor(id)) {
                    insertNodeConfigBySensorAddress(ID.getAddress(id), deviceId)
                } else {
                    insertNodeConfigByMeasurementId(deviceId, id)
                }
            }
        }
        return true
    }

    override fun onItemsSelected(dialog: ListDialog, positions: IntArray) {
        when (dialog.tag) {
            DIALOG_TAG_INSERT_NODES -> {
                if (positions.isNotEmpty()) {
                    val deviceId = getDeviceId()
                    val addresses = getConfiguredSensorAddresses()
                    positions.forEach {
                        insertNodeConfigBySensorAddress(addresses[it].toInt(16), deviceId)
                    }
                }
            }
        }
    }

    private fun insertNodeConfigBySensorAddress(address: Int, deviceId: Long) {
        val sensor = SensorManager.getPhysicalSensor(address)
        for (i in 0 until sensor.displayMeasurementSize) {
            val measurement = sensor.getDisplayMeasurementByPosition(i)
            insertNodeConfigByMeasurementId(deviceId, measurement.id.id)
        }
    }

    private fun insertNodeConfigByMeasurementId(deviceId: Long, measurementId: Long) {
        val values = ContentValues()
        values.put(Constant.COLUMN_DEVICE_ID, deviceId)
        values.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, measurementId)
        databaseHandler.startInsert(TOKEN_INSERT_NODE_CONFIG,
                measurementId, Constant.TABLE_NODE, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeleteComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_DELETE_NODE_CONFIG -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    adapter.scheduleItemRemove(position)
                    refreshDeviceConfig()
                    if (!adapter.hasDeletingItems()) {
                        adapter.scheduleItemRangeChange(0, adapter.itemCount)
                    }
                } else {
                    SimpleCustomizeToast.show(R.string.delete_node_config_failed)
                }
            }
        }
    }

    override fun onReplaceComplete(token: Int, cookie: Any?, rowId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExecSqlComplete(token: Int, cookie: Any?, result: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInsertComplete(token: Int, cookie: Any?, rowId: Long) {
        when (token) {
            TOKEN_INSERT_NODE_CONFIG -> {
                if (rowId != -1L) {
                    val measurementId = cookie as Long
                    val position = adapter.findNodeConfigById(measurementId)
                    adapter.scheduleItemInsert(-position - 1)
                    refreshDeviceConfig()
                } else {
                    SimpleCustomizeToast.show(R.string.add_node_config_failed)
                }
            }
        }
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        if (affectedRowCount > 0) {
            if (cookie is String) {
                intent.putExtra(Constant.COLUMN_DEVICE_NAME, cookie)
                updateBaseInfo()
            } else {
                if (cookie is Int) {
                    adapter.scheduleItemChange(cookie)
                }
                refreshDeviceConfig()
            }
        } else {
            val errStringRes = when (token) {
                TOKEN_UPDATE_DEVICE_NAME ->  R.string.device_name_modify_failed
                TOKEN_UPDATE_NODE_NAME -> R.string.node_name_modify_failed
                else -> 0
            }
            if (errStringRes != 0) {
                SimpleCustomizeToast.show(errStringRes)
            }
        }
    }

    private fun refreshDeviceConfig() {
        supportLoaderManager.getLoader<Cursor>(0).onContentChanged()
    }
}
