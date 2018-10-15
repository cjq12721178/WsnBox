package com.weisi.tool.wsnbox.fragment.config

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.ID
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.EditDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.cjq.tool.qbox.ui.loader.SimpleCursorLoader
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.activity.SensorConfigurationActivity
import com.weisi.tool.wsnbox.adapter.config.SensorsConfigAdapter
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import kotlinx.android.synthetic.main.fragment_sensor_configuration.view.*

class SensorConfigurationFragment : ConfigurationFragment(),
        LoaderManager.LoaderCallbacks<Cursor>,
        BaseDialog.OnDialogConfirmListener,
        BaseDialog.OnDialogCancelListener,
        EditDialog.OnContentReceiver,
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener {

    private val LOADER_ID = 1;
    private val TOKEN_DELETE_SENSOR_CONFIG = 1
    private val TOKEN_ADD_SENSOR_CONFIG = 2
    private val DIALOG_TAG_CONFIRM_DELETE_SENSOR_CONFIG = "del_sns_cfg"
    private val DIALOG_TAG_ADD_SENSOR_CONFIG = "add_sensor_cfg"
    private val DIALOG_TAG_SENSOR_CONFIG_EXISTS = "sensor_cfg_exists"
    private val ARGUMENT_KEY_SENSOR_CONFIG_POSITION = "sensor_cfg_pos"
    private val ARGUMENT_KEY_NEW_SENSOR_CONFIG_POSITION = "new_sns_cfg_pos"
    private val REQUEST_CODE_UPDATE_SENSOR_NAME = 1

    private val adapter = SensorsConfigAdapter()
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sensor_configuration, null)
        view.rv_sensors.layoutManager = LinearLayoutManager(context)
        view.rv_sensors.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(view.rv_sensors) {
            override fun onItemClick(v: View?, position: Int) {
                if (adapter.inDeleteMode) {
                    adapter.selectDeletingItem(position)
                    adapter.notifyItemChanged(position)
                } else {
                    startSensorConfigurationActivity(position)
                }
            }
        })
        view.rv_sensors.adapter = adapter

        activity?.supportLoaderManager?.initLoader(LOADER_ID, null, this)
        return view
    }

    private fun startSensorConfigurationActivity(position: Int) {
        val intent = Intent(context, SensorConfigurationActivity::class.java)
        intent.putExtra(Constant.COLUMN_SENSOR_CONFIGURATION_ID, adapter.getItemId(position))
        startActivityForResult(intent, REQUEST_CODE_UPDATE_SENSOR_NAME)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_UPDATE_SENSOR_NAME) {
            refreshSensorConfigurationList()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return object : SimpleCursorLoader(context!!) {
            override fun loadInBackground(): Cursor? {
                return SensorDatabase.importSensorsConfiguration(getConfigurationProviderId())
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        adapter.swapCursor(data)
        startNewSensorConfigurationActivityIfPossible()
    }

    private fun startNewSensorConfigurationActivityIfPossible() {
        val position = activity?.intent?.getIntExtra(ARGUMENT_KEY_NEW_SENSOR_CONFIG_POSITION, -1) ?: -1
        if (position != -1) {
            activity!!.intent.putExtra(ARGUMENT_KEY_NEW_SENSOR_CONFIG_POSITION, -1)
            startSensorConfigurationActivity(position)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        adapter.changeCursor(null)
    }

    override fun onAdd() {
        val dialog = EditDialog()
        dialog.setTitle(R.string.input_sensor_address)
        dialog.show(childFragmentManager, DIALOG_TAG_ADD_SENSOR_CONFIG)
    }

    override fun onDelete() {
        if (adapter.inDeleteMode) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.confirm_delete_sensor_config)
            dialog.show(childFragmentManager, DIALOG_TAG_CONFIRM_DELETE_SENSOR_CONFIG)
        } else {
            adapter.changeDeleteModeWithNotification()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            activity?.intent?.putExtra(Constant.TAG_ADDRESSES,
                    adapter.getSensorAddresses())
        }
    }

    override fun onConfirm(dialog: BaseDialog<*>?): Boolean {
        when (dialog?.tag) {
            DIALOG_TAG_CONFIRM_DELETE_SENSOR_CONFIG -> {
                adapter.getDeletingItemsPosition().forEach {
                    databaseHandler.startDelete(TOKEN_DELETE_SENSOR_CONFIG,
                            it, Constant.TABLE_SENSOR_CONFIGURATION,
                            "${Constant.COLUMN_COMMON_ID} = ?",
                            arrayOf(adapter.getItemId(it).toString()))
                }
                adapter.inDeleteMode = !adapter.inDeleteMode
            }
            DIALOG_TAG_SENSOR_CONFIG_EXISTS -> {
                startSensorConfigurationActivity(dialog.arguments!!.getInt(ARGUMENT_KEY_SENSOR_CONFIG_POSITION))
            }
        }
        return true
    }

    override fun onCancel(dialog: BaseDialog<*>?) {
        when (dialog?.tag) {
            DIALOG_TAG_CONFIRM_DELETE_SENSOR_CONFIG -> {
                adapter.changeDeleteModeWithNotification()
            }
        }
    }

    override fun onReceive(dialog: EditDialog?, oldValue: String?, newValue: String?): Boolean {
        when (dialog?.tag) {
            DIALOG_TAG_ADD_SENSOR_CONFIG -> {
                return addSensorConfig(newValue)
            }
        }
        return true
    }

    private fun addSensorConfig(addressStr: String?): Boolean {
        if (addressStr.isNullOrEmpty()) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.sensor_address_empty)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager, "sns_addr_null")
            return false
        }
        val addressInt = addressStr!!.toIntOrNull(16)
        if (addressInt === null) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.sensor_address_parse_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager, "sns_addr_err")
            return false
        }
        val address = ID.getAddress(ID.getId(addressInt))
        val position = adapter.findSensorConfigByAddress(address)
        if (position >= 0) {
            val dialog = ConfirmDialog()
            dialog.arguments?.putInt(ARGUMENT_KEY_SENSOR_CONFIG_POSITION, position)
            dialog.setTitle(R.string.sensor_config_already_exists)
            dialog.show(childFragmentManager, DIALOG_TAG_SENSOR_CONFIG_EXISTS)
            return false
        }
        val values = ContentValues()
        values.put(Constant.COLUMN_CONFIGURATION_PROVIDER_ID, getConfigurationProviderId())
        values.put(Constant.COLUMN_SENSOR_ADDRESS, address)
        databaseHandler.startInsert(TOKEN_ADD_SENSOR_CONFIG,
                -position - 1,
                Constant.TABLE_SENSOR_CONFIGURATION,
                values,
                SQLiteDatabase.CONFLICT_NONE)
        return true
    }

//    private fun sensorConfigExits(address: Int): Int {
//        for (i in 0 until adapter.itemCount) {
//            if (address == adapter.getSensorAddress(i)) {
//                return i
//            }
//        }
//        return -1
//    }

    private fun refreshSensorConfigurationList() {
        activity?.supportLoaderManager?.getLoader<Cursor>(LOADER_ID)?.onContentChanged()
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeleteComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_DELETE_SENSOR_CONFIG -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    adapter.scheduleItemRemove(position)
                    refreshSensorConfigurationList()
                    if (!adapter.hasDeletingItems()) {
                        adapter.scheduleItemRangeChange(0, adapter.itemCount)
                    }
                } else {
                    SimpleCustomizeToast.show(R.string.delete_sensor_config_failed)
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
            TOKEN_ADD_SENSOR_CONFIG -> {
                if (rowId != -1L) {
                    val position = cookie as Int
                    adapter.scheduleItemInsert(position)
                    activity?.intent?.putExtra(ARGUMENT_KEY_NEW_SENSOR_CONFIG_POSITION, position)
                    refreshSensorConfigurationList()
                } else {
                    SimpleCustomizeToast.show(R.string.add_sensor_config_failed)
                }
            }
        }
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {

    }
}