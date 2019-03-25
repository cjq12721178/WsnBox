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
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.EditDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.activity.DeviceConfigurationActivity
import com.weisi.tool.wsnbox.adapter.config.DevicesConfigAdapter
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.processor.loader.DevicesConfigInfoLoader
import kotlinx.android.synthetic.main.fragment_device_configuration.view.*

class DeviceConfigurationFragment : ConfigurationFragment(),
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        BaseDialog.OnDialogConfirmListener,
        BaseDialog.OnDialogCancelListener,
        EditDialog.OnContentReceiver, View.OnClickListener {

    private val LOADER_ID = 2
    private val DIALOG_TAG_ADD_DEVICE_CONFIG = "add_dev_cfg"
    private val DIALOG_TAG_DEVICE_CONFIG_EXISTS = "dev_cfg_exist"
    private val DIALOG_TAG_CONFIRM_DELETE_DEVICE_CONFIG = "del_dev_cfg"
    private val TOKEN_ADD_DEVICE_CONFIG = 1
    private val TOKEN_DELET_DEVICE_CONFIG = 2
    private val ARGUMENT_KEY_DEVICE_CONFIG_POSITION = "dev_cfg_pos"
    private val ARGUMENT_KEY_NEW_DEVICE_CONFIG_POSITION = "new_dev_cfg_pos"
    private val REQUEST_CODE_UPDATE_DEVICE_NAME = 2

    private val adapter = DevicesConfigAdapter()
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_configuration, null)
        view.btn_add.setOnClickListener(this)
        view.btn_delete.setOnClickListener(this)
        view.rv_devices.layoutManager = LinearLayoutManager(context)
        view.rv_devices.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(view.rv_devices) {
            override fun onItemClick(v: View?, position: Int) {
                if (adapter.inDeleteMode) {
                    adapter.selectDeletingItem(position)
                    adapter.notifyItemChanged(position)
                } else {
                    startDeviceConfigurationActivity(position)
                }
            }
        })
        view.rv_devices.adapter = adapter

        activity?.supportLoaderManager?.initLoader(LOADER_ID, null, this)
        return view
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return DevicesConfigInfoLoader(context!!, getConfigurationProviderId())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        adapter.swapCursor(data)
        startNewDeviceConfigurationActivityIfPossible()
    }

    private fun startNewDeviceConfigurationActivityIfPossible() {
        val position = activity?.intent?.getIntExtra(ARGUMENT_KEY_NEW_DEVICE_CONFIG_POSITION, -1) ?: -1
        if (position != -1) {
            activity!!.intent.putExtra(ARGUMENT_KEY_NEW_DEVICE_CONFIG_POSITION, -1)
            startDeviceConfigurationActivity(position)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.changeCursor(null)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_add -> {
                val dialog = EditDialog()
                dialog.setTitle(R.string.input_device_name)
                dialog.show(childFragmentManager, DIALOG_TAG_ADD_DEVICE_CONFIG)
            }
            R.id.btn_delete -> {
                if (adapter.inDeleteMode) {
                    val dialog = ConfirmDialog()
                    dialog.setTitle(R.string.confirm_delete_device_config)
                    dialog.show(childFragmentManager, DIALOG_TAG_CONFIRM_DELETE_DEVICE_CONFIG)
                } else {
                    adapter.changeDeleteModeWithNotification()
                }
            }
        }
    }

    override fun onReceive(dialog: EditDialog, oldValue: String?, newValue: String?): Boolean {
        when (dialog.tag) {
            DIALOG_TAG_ADD_DEVICE_CONFIG -> {
                return addDeviceConfig(newValue)
            }
        }
        return true
    }

    private fun addDeviceConfig(name: String?): Boolean {
        if (name.isNullOrEmpty()) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.device_name_empty)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager, "dev_name_null")
            return false
        }
        val position = adapter.findDeviceConfigByName(name)
        if (position >= 0) {
            val dialog = ConfirmDialog()
            dialog.arguments?.putInt(ARGUMENT_KEY_DEVICE_CONFIG_POSITION, position)
            dialog.setTitle(R.string.device_config_already_exists)
            dialog.show(childFragmentManager, DIALOG_TAG_DEVICE_CONFIG_EXISTS)
            return false
        }
        val values = ContentValues()
        values.put(Constant.COLUMN_CONFIGURATION_PROVIDER_ID, getConfigurationProviderId())
        values.put(Constant.COLUMN_DEVICE_NAME, name)
        databaseHandler.startInsert(TOKEN_ADD_DEVICE_CONFIG,
                -position-1,
                Constant.TABLE_DEVICE,
                values,
                SQLiteDatabase.CONFLICT_NONE)
        return true
    }

    override fun onConfirm(dialog: BaseDialog<*>): Boolean {
        when (dialog.tag) {
            DIALOG_TAG_CONFIRM_DELETE_DEVICE_CONFIG -> {
                adapter.getDeletingItemsPosition().forEach() {
                    databaseHandler.startDelete(TOKEN_DELET_DEVICE_CONFIG,
                            it, Constant.TABLE_DEVICE,
                            "${Constant.COLUMN_COMMON_ID} = ?",
                            arrayOf(adapter.getItemId(it).toString()))
                }
                adapter.inDeleteMode = !adapter.inDeleteMode
            }
            DIALOG_TAG_DEVICE_CONFIG_EXISTS -> {
                startDeviceConfigurationActivity(dialog.arguments!!.getInt(ARGUMENT_KEY_DEVICE_CONFIG_POSITION))
            }
        }
        return true
    }

    private fun startDeviceConfigurationActivity(position: Int) {
        val intent = Intent(context, DeviceConfigurationActivity::class.java)
        intent.putExtra(Constant.COLUMN_COMMON_ID, adapter.getItemId(position))
        intent.putExtra(Constant.COLUMN_DEVICE_NAME, adapter.getDeviceName(position))
        intent.putExtra(Constant.TAG_NAMES, adapter.getDevicesName())
        intent.putExtra(Constant.TAG_ADDRESSES, activity?.intent?.getStringArrayExtra(Constant.TAG_ADDRESSES))
        startActivityForResult(intent, REQUEST_CODE_UPDATE_DEVICE_NAME)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_UPDATE_DEVICE_NAME) {
            refreshDeviceConfigurationList()
        }
    }

    override fun onCancel(dialog: BaseDialog<*>) {
        when (dialog.tag) {
            DIALOG_TAG_CONFIRM_DELETE_DEVICE_CONFIG -> {
                adapter.changeDeleteModeWithNotification()
            }
        }
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeleteComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_DELET_DEVICE_CONFIG -> {
                if (affectedRowCount > 0) {
                    val position = cookie as Int
                    adapter.scheduleItemRemove(position)
                    refreshDeviceConfigurationList()
                    if (!adapter.hasDeletingItems()) {
                        adapter.scheduleItemRangeChange(0, adapter.itemCount)
                    }
                } else {
                    SimpleCustomizeToast.show(R.string.delete_device_config_failed)
                }
            }
        }
    }

    private fun refreshDeviceConfigurationList() {
        activity?.supportLoaderManager?.getLoader<Cursor>(LOADER_ID)?.onContentChanged()
    }

    override fun onReplaceComplete(token: Int, cookie: Any?, rowId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExecSqlComplete(token: Int, cookie: Any?, result: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInsertComplete(token: Int, cookie: Any?, rowId: Long) {
        when (token) {
            TOKEN_ADD_DEVICE_CONFIG -> {
                if (rowId != -1L) {
                    val position = cookie as Int
                    adapter.scheduleItemInsert(position)
                    activity?.intent?.putExtra(ARGUMENT_KEY_NEW_DEVICE_CONFIG_POSITION, position)
                    refreshDeviceConfigurationList()
                } else {
                    SimpleCustomizeToast.show(R.string.add_device_config_failed)
                }
            }
        }
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}