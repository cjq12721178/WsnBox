package com.weisi.tool.wsnbox.activity


import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.cjq.lib.weisi.iot.ID
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler
import com.cjq.tool.qbox.ui.adapter.HeaderAndFooterWrapper
import com.cjq.tool.qbox.ui.dialog.BaseDialog
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.EditDialog
import com.cjq.tool.qbox.ui.dialog.ListDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.config.SensorConfigAdapter
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.processor.loader.SensorConfigurationLoader
import kotlinx.android.synthetic.main.activity_sensor_configuration.*
import kotlinx.android.synthetic.main.lh_sensor_base_config.view.*

class SensorConfigurationActivity : BaseActivity(),
        LoaderManager.LoaderCallbacks<SensorConfiguration>,
        BaseDialog.OnDialogConfirmListener,
        EditDialog.OnContentReceiver,
        ListDialog.OnItemSelectedListener,
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener {

    private val TOKEN_UPDATE_SENSOR_CUSTOM_NAME = 1
    private val TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME = 2
    private val TOKEN_ADD_MEASUREMENT_CONFIG = 3
    private val TOKEN_UPDATE_WARNER_TYPE = 4
    private val TOKEN_DELETE_WARNER_TYPE = 5
    private val TOKEN_INSERT_WARNER_TYPE = 6
    private val TOKEN_QUERY_MEASUREMENT_CONFIG_ID = 7
    private val TOKEN_UPDATE_HIGH_LIMIT = 8
    private val TOKEN_UPDATE_LOW_LIMIT = 9
    private val TOKEN_UPDATE_ABNORMAL_VALUE = 10
    private val TOKEN_UPDATE_INITIAL_VALUE = 11
    private val TOKEN_UPDATE_INITIAL_DISTANCE = 12
    private val DIALOG_TAG_MODIFY_SENSOR_CUSTOM_NAME = "mdf_sns_cus_name"
    private val DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME = "mdf_msm_cus_name"
    private val DIALOG_TAG_CHOOSE_WARNER_TYPE = "mdf_warner_type"
    private val DIALOG_TAG_MODIFY_VALUE = "mdf_warner_value"
    private val ARGUMENT_KEY_TOKEN = "token"
    private val ARGUMENT_KEY_MEASUREMENT_CONFIG_ID = "msm_cfg_id"
    private val ARGUMENT_KEY_MEASUREMENT_ID = "msm_id"
    private val ARGUMENT_KEY_MEASUREMENT_TYPE = "msm_type"
    private val ARGUEMENT_KEY_WARNER_TYPE = "warner_type"
    private val ARGUMENT_KEY_COLUMN_NAME = "col_name"
    private val ARGUMENT_KEY_COLUMN_VALUE = "col_value"
    //private val ARGUMENT_KEY_WARNER_VALUE = "warner_value"
    private val ARGUMENT_KEY_TABLE_NAME = "tb_name"
    //private val DIALOG_TAG_IMPORT_SENSOR_CONFIG_FAILED = "import_sensor_cfg_err"
    private val adapter = SensorConfigAdapter()
    private lateinit var vBaseInfo: View
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_configuration)

//        if (savedInstanceState !== null) {
//            intent.putExtra(Constant.COLUMN_SENSOR_CONFIGURATION_ID, savedInstanceState.getLong(Constant.COLUMN_SENSOR_CONFIGURATION_ID))
//        }

        rv_sensor_config.layoutManager = LinearLayoutManager(this)
        rv_sensor_config.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(rv_sensor_config) {
            override fun onItemClick(v: View?, position: Int) {
                val measure = adapter.getItemByPosition(position)
                when (v?.id) {
                    R.id.tv_custom_name_value -> {
                        val dialog = EditDialog()
                        dialog.setTitle(R.string.modify_measurement_custom_name)
                        dialog.setContent(measure.configuration.decorator?.decorateName(null))
                        putDialogArgumentsForMeasure(dialog, measure)
                        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME)
                    }
                    R.id.tv_warner_type_value -> {
                        val dialog = ListDialog()
                        dialog.setTitle(R.string.choose_warner_type)
                        dialog.setItems(resources.getStringArray(R.array.warner_types))
                        putDialogArgumentsForMeasure(dialog, measure)
                        dialog.show(supportFragmentManager, DIALOG_TAG_CHOOSE_WARNER_TYPE)
                    }
                    R.id.tv_high_limit_value -> {
                        showValueSetDialog(R.string.set_high_limit,
                                v, measure, TOKEN_UPDATE_HIGH_LIMIT,
                                Constant.TABLE_GENERAL_SINGLE_RANGE_WARNER,
                                Constant.COLUMN_HIGH_LIMIT)
                    }
                    R.id.tv_low_limit_value -> {
                        showValueSetDialog(R.string.set_low_limit,
                                v, measure, TOKEN_UPDATE_LOW_LIMIT,
                                Constant.TABLE_GENERAL_SINGLE_RANGE_WARNER,
                                Constant.COLUMN_LOW_LIMIT)
                    }
                    R.id.tv_abnormal_value -> {
                        showValueSetDialog(R.string.set_abnormal_value,
                                v, measure, TOKEN_UPDATE_ABNORMAL_VALUE,
                                Constant.TABLE_GENERAL_SWITCH_WARNER,
                                Constant.COLUMN_ABNORMAL_VALUE)
                    }
                    R.id.tv_initial_value_value -> {
                        showValueSetDialog(R.string.set_initial_value,
                                v, measure, TOKEN_UPDATE_INITIAL_VALUE,
                                Constant.TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION,
                                Constant.COLUMN_INITIAL_VALUE)
                    }
                    R.id.tv_initial_distance_value -> {
                        showValueSetDialog(R.string.set_initial_distance,
                                v, measure, TOKEN_UPDATE_INITIAL_DISTANCE,
                                Constant.TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION,
                                Constant.COLUMN_INITIAL_DISTANCE)
                    }
                }
            }
        }.addItemChildViewTouchEnabled(R.id.tv_custom_name_value)
                .addItemChildViewTouchEnabled(R.id.tv_warner_type_value)
                .addItemChildViewTouchEnabled(R.id.tv_high_limit_value)
                .addItemChildViewTouchEnabled(R.id.tv_low_limit_value)
                .addItemChildViewTouchEnabled(R.id.tv_abnormal_value)
                .addItemChildViewTouchEnabled(R.id.tv_initial_value_value)
                .addItemChildViewTouchEnabled(R.id.tv_initial_distance_value))

        val wrapper = HeaderAndFooterWrapper(adapter)
        val inflater = LayoutInflater.from(this)
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_sensor_config, R.string.basic_info))
        wrapper.addHeaderView(getBaseInfoView(inflater))
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_sensor_config, R.string.measurement_list))
        rv_sensor_config.adapter = wrapper

        supportLoaderManager.initLoader(0, null, this)
    }

    private fun showValueSetDialog(titleStringRes: Int,
                                   view: View,
                                   measure: SensorConfiguration.Measure,
                                   token: Int,
                                   tableName: String,
                                   columnName: String) {
        val dialog = EditDialog()
        dialog.setTitle(titleStringRes)
        dialog.setContent((view as TextView).text.toString())
        putDialogArgumentsForValueUpdate(dialog,
                measure.configurationId,
                measure.id,
                token,
                tableName,
                columnName)
        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_VALUE)
    }

    private fun putDialogArgumentsForMeasure(dialog: BaseDialog<*>, measure: SensorConfiguration.Measure) {
        dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measure.configurationId)
        dialog.arguments?.putInt(ARGUMENT_KEY_MEASUREMENT_TYPE, measure.type)
        if (measure.configurationId == 0L) {
            dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measure.id)
        }
    }

    private fun putDialogArgumentsForValueUpdate(dialog: BaseDialog<*>,
                                                 configurationId: Long,
                                                 measurementId: Long,
                                                 token: Int,
                                                 tableName: String,
                                                 columnName: String) {
        dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, configurationId)
        dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measurementId)
        dialog.arguments?.putInt(ARGUMENT_KEY_TOKEN, token)
        dialog.arguments?.putString(ARGUMENT_KEY_TABLE_NAME, tableName)
        dialog.arguments?.putString(ARGUMENT_KEY_COLUMN_NAME, columnName)
    }

    private fun getBaseInfoView(inflater: LayoutInflater): View {
        vBaseInfo = inflater.inflate(R.layout.lh_sensor_base_config, rv_sensor_config, false)
        vBaseInfo.tv_custom_name_value.setOnClickListener() {
            val dialog = EditDialog()
            dialog.setTitle(R.string.modify_sensor_custom_name)
            dialog.setContent(adapter.sensorConfig?.base?.configuration?.decorator?.decorateName(null))
            dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_SENSOR_CUSTOM_NAME)
        }
        return vBaseInfo
    }

//    override fun onSaveInstanceState(outState: Bundle?) {
//        outState?.putLong(Constant.COLUMN_SENSOR_CONFIGURATION_ID, getSensorConfigId())
//        super.onSaveInstanceState(outState)
//    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<SensorConfiguration> {
        return SensorConfigurationLoader(this, getSensorConfigId())
    }

    private fun getSensorConfigId() = intent.getLongExtra(Constant.COLUMN_SENSOR_CONFIGURATION_ID, -1)

    override fun onLoadFinished(loader: Loader<SensorConfiguration>, data: SensorConfiguration?) {
        updateBasicInfo(data)
        updateMeasureList(data)
    }

    private fun updateBasicInfo(sensorConfiguration: SensorConfiguration?) {
        vBaseInfo.tv_address_value.text = if (sensorConfiguration === null) {
            null
        } else {
            ID.getFormatAddress(sensorConfiguration.base.address)
        }
        vBaseInfo.tv_default_name_value.text = sensorConfiguration?.base?.defaultName
        vBaseInfo.tv_custom_name_value.text = sensorConfiguration?.base?.configuration?.decorator?.decorateName(null)
    }

    private fun updateMeasureList(sensorConfiguration: SensorConfiguration?) {
        val previousSize = adapter.itemCount
        adapter.sensorConfig = sensorConfiguration
        adapter.notifyDataSetChanged(previousSize)
    }

    override fun onLoaderReset(loader: Loader<SensorConfiguration>) {
        updateBasicInfo(null)
        updateMeasureList(null)
    }

    override fun onConfirm(dialog: BaseDialog<*>?): Boolean {
        when (dialog?.tag) {
//            DIALOG_TAG_IMPORT_SENSOR_CONFIG_FAILED -> {
//                setResult(Activity.RESULT_CANCELED)
//                finish()
//            }
        }
        return true
    }

    override fun onReceive(dialog: EditDialog?, oldValue: String?, newValue: String?): Boolean {
        if (TextUtils.equals(oldValue, newValue)) {
            return true
        }
        when (dialog?.tag) {
            DIALOG_TAG_MODIFY_SENSOR_CUSTOM_NAME -> {
                val values = ContentValues()
                values.put(Constant.COLUMN_CUSTOM_NAME, newValue)
                databaseHandler.startUpdate(TOKEN_UPDATE_SENSOR_CUSTOM_NAME,
                    null, Constant.TABLE_SENSOR_CONFIGURATION,
                    values, "${Constant.COLUMN_COMMON_ID} = ?",
                    arrayOf(getSensorConfigId().toString()), SQLiteDatabase.CONFLICT_NONE)
            }
            DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME -> {
                val measurementConfigId = getMeasurementConfigId(dialog)
                if (measurementConfigId == 0L) {
                    addMeasurementConfigWithCustomName(dialog, newValue)
                } else {
                    val values = ContentValues()
                    values.put(Constant.COLUMN_CUSTOM_NAME, newValue)
                    databaseHandler.startUpdate(TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME,
                            null, Constant.TABLE_MEASUREMENT_CONFIGURATION,
                            values, "${Constant.COLUMN_COMMON_ID} = ?",
                            arrayOf(getMeasurementConfigId(dialog).toString()),
                    SQLiteDatabase.CONFLICT_NONE)
                }
            }
            DIALOG_TAG_MODIFY_VALUE -> {
                if (newValue.isNullOrEmpty()) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.input_value_empty)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, "input_value_empty")
                    return false
                }
                val value = newValue.toDoubleOrNull()
                if (value === null) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.input_value_parse_failed)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, "input_value_err")
                    return false
                }
                val args = Bundle(dialog.arguments)
                args.putDouble(ARGUMENT_KEY_COLUMN_VALUE, value)
                if (args.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID) == 0L) {
                    addMeasurementConfig(dialog, args)
                } else {
                    //insertColumnValue(dialog.arguments!!)
                    queryFunctionId(args)
                }
            }
        }
        return true
    }

    private fun updateColumnValue(args: Bundle) {
        val values = ContentValues()
        values.put(args.getString(ARGUMENT_KEY_COLUMN_NAME),
                args.getDouble(ARGUMENT_KEY_COLUMN_VALUE))
        databaseHandler.startUpdate(args.getInt(ARGUMENT_KEY_TOKEN),
        null, args.getString(ARGUMENT_KEY_TABLE_NAME),
        values, "${Constant.COLUMN_COMMON_ID} = ?",
        arrayOf(args.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID).toString()),
        SQLiteDatabase.CONFLICT_NONE)
    }

    private fun insertColumnValue(args: Bundle, values: ContentValues) {
        values.put(Constant.COLUMN_COMMON_ID,
                args.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID))
        values.put(args.getString(ARGUMENT_KEY_COLUMN_NAME),
                args.getDouble(ARGUMENT_KEY_COLUMN_VALUE))
        databaseHandler.startInsert(args.getInt(ARGUMENT_KEY_TOKEN),
                null, args.getString(ARGUMENT_KEY_TABLE_NAME),
                values, SQLiteDatabase.CONFLICT_REPLACE)
    }

//    private fun canInputValueQualified(valueStr: String?): Boolean {
//        if (!valueStr.isNullOrEmpty()) {
//            valueStr.toDoubleOrNull()
//        }
//    }

    private fun addMeasurementConfigWithCustomName(dialog: BaseDialog<*>, customName: String?) {
        return addMeasurementConfig(dialog, null, customName)
    }

    private fun addMeasurementConfig(dialog: BaseDialog<*>, args: Bundle?) {
        return addMeasurementConfig(dialog, args, null)
    }

    private fun addMeasurementConfig(dialog: BaseDialog<*>, args: Bundle?, customName: String?) {
//        val values = ContentValues()
//        values.put(Constant.COLUMN_SENSOR_CONFIGURATION_ID, getSensorConfigId())
//        values.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, getMeasurementId(dialog))
//        val type = getMeasurementType(dialog)
//        if (type != 0) {
//            values.put(Constant.COLUMN_TYPE, type)
//        }
//        if (args === null) {
//            values.put(Constant.COLUMN_CUSTOM_NAME, customName)
//        }
//        databaseHandler.startInsert(TOKEN_ADD_MEASUREMENT_CONFIG,
//                args, Constant.TABLE_MEASUREMENT_CONFIGURATION,
//                values, SQLiteDatabase.CONFLICT_NONE)
        return addMeasurementConfig(getMeasurementId(dialog), getMeasurementType(dialog), args, customName)
    }

    private fun addMeasurementConfig(measurementId: Long, measurementType: Int, args: Bundle?, customName: String?) {
        val values = ContentValues()
        values.put(Constant.COLUMN_SENSOR_CONFIGURATION_ID, getSensorConfigId())
        values.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, measurementId)
        val type = SensorConfiguration.Measure.getConfigType(measurementType)
        if (type != SensorConfiguration.Measure.CT_NORMAL) {
            values.put(Constant.COLUMN_TYPE, type)
        }
        if (args === null) {
            values.put(Constant.COLUMN_CUSTOM_NAME, customName)
        }
        databaseHandler.startInsert(TOKEN_ADD_MEASUREMENT_CONFIG,
                args, Constant.TABLE_MEASUREMENT_CONFIGURATION,
                values, SQLiteDatabase.CONFLICT_NONE)
    }

    override fun onItemSelected(dialog: ListDialog, position: Int, items: Array<out Any>) {
        when (dialog.tag) {
            DIALOG_TAG_CHOOSE_WARNER_TYPE -> {
                val oldMeasureType = getMeasurementType(dialog)
                val newWarnerType = SensorConfiguration.Measure.getWarnerType(SensorConfiguration.Measure.buildType(position, 0))
                val oldWarnerType = SensorConfiguration.Measure.getWarnerType(oldMeasureType)
                if (newWarnerType == oldWarnerType) {
                    return
                }
                val measurementConfigId = getMeasurementConfigId(dialog)
                if (oldWarnerType != SensorConfiguration.Measure.WT_NONE) {
                    if (measurementConfigId != 0L) {
                        val args = if (newWarnerType != SensorConfiguration.Measure.WT_NONE) {
                            val bundle = Bundle()
                            bundle.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
                            bundle.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measurementConfigId)
                            bundle.putInt(ARGUEMENT_KEY_WARNER_TYPE, newWarnerType)
                            bundle
                        } else {
                            null
                        }
                        databaseHandler.startDelete(TOKEN_DELETE_WARNER_TYPE,
                                args, getWarnerTableName(oldWarnerType),
                                "${Constant.COLUMN_COMMON_ID} = ?",
                                arrayOf(measurementConfigId.toString()))
                    } else {
                        throw IllegalArgumentException("warner type exist while measurement does not")
                    }
                } else {
                    if (newWarnerType != SensorConfiguration.Measure.WT_NONE) {
                        insertWarnerType(measurementConfigId,
                                getMeasurementId(dialog),
                                SensorConfiguration.Measure.buildType(newWarnerType,
                                        SensorConfiguration.Measure.getConfigType(oldMeasureType)))
                    }
                }
            }
        }
    }

    private fun insertWarnerType(measurementConfigId: Long, measurementId: Long, measureType: Int) {
        val warnerType = SensorConfiguration.Measure.getWarnerType(measureType)
        if (measurementConfigId == 0L) {
            val args = Bundle()
            args.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
            args.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measurementId)
            args.putInt(ARGUEMENT_KEY_WARNER_TYPE, warnerType)
            addMeasurementConfig(measurementId, measureType, args, null)
        } else {
            insertWarnerType(measurementConfigId, warnerType)
        }
    }

//    private fun buildInsertWarnerTypeArguments(measurementConfigId: Long, warnerType: Int): Bundle {
//        val args = Bundle()
//        args.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
//        args.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measurementConfigId)
//        args.putInt(ARGUEMENT_KEY_WARNER_TYPE, warnerType)
//        return args
//    }

    private fun insertWarnerType(measurementConfigId: Long, warnerType: Int) {
        val values = ContentValues()
        values.put(Constant.COLUMN_COMMON_ID, measurementConfigId)
        when (warnerType) {
            SensorConfiguration.Measure.WT_SINGLE_RANGE -> {
                values.put(Constant.COLUMN_LOW_LIMIT, 0.0)
                values.put(Constant.COLUMN_HIGH_LIMIT, 1.0)
            }
            SensorConfiguration.Measure.WT_SWITCH -> {
                values.put(Constant.COLUMN_ABNORMAL_VALUE, 0.0)
            }
        }
        databaseHandler.startInsert(TOKEN_INSERT_WARNER_TYPE,
                null, getWarnerTableName(warnerType),
                values, SQLiteDatabase.CONFLICT_NONE)
    }

    private fun insertWarnerType(args: Bundle) {
        insertWarnerType(args.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID), args.getInt(ARGUEMENT_KEY_WARNER_TYPE))
    }

    private fun getWarnerTableName(warnerType: Int): String {
        return when (warnerType) {
            SensorConfiguration.Measure.WT_SINGLE_RANGE -> Constant.TABLE_GENERAL_SINGLE_RANGE_WARNER
            SensorConfiguration.Measure.WT_SWITCH -> Constant.TABLE_GENERAL_SWITCH_WARNER
            else -> getString(R.string.warner_type_none)
        }
    }

    private fun getMeasurementId(dialog: BaseDialog<*>) =
            dialog.arguments?.getLong(ARGUMENT_KEY_MEASUREMENT_ID, 0L) ?: 0L

    private fun getMeasurementConfigId(dialog: BaseDialog<*>) =
            dialog.arguments?.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, 0L) ?: 0L

    private fun getMeasurementType(dialog: BaseDialog<*>) =
            dialog.arguments?.getInt(ARGUMENT_KEY_MEASUREMENT_TYPE, 0) ?: 0

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
        when (token) {
            TOKEN_QUERY_MEASUREMENT_CONFIG_ID -> {
                if (cookie is Bundle) {
                    val innerToken = cookie.getInt(ARGUMENT_KEY_TOKEN)
                    if (innerToken == TOKEN_INSERT_WARNER_TYPE) {
                        if (cursor?.moveToNext() == true) {
                            cookie.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, cursor.getLong(cursor.getColumnIndex(Constant.COLUMN_COMMON_ID)))
                            insertWarnerType(cookie)
                        } else {
                            SimpleCustomizeToast.show(R.string.warner_type_modify_failed)
                        }
                    } else {
                        if (cursor?.moveToNext() == true) {
                            cookie.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, cursor.getLong(cursor.getColumnIndex(Constant.COLUMN_MEASUREMENT_CONFIGURATION_ID)))
                            if (cursor.getLong(cursor.getColumnIndex(Constant.COLUMN_FUNCTION_ID)) == 0L) {
                                val values = ContentValues()
                                when (innerToken) {
                                    TOKEN_UPDATE_INITIAL_VALUE, TOKEN_UPDATE_INITIAL_DISTANCE -> {
                                        values.put(Constant.COLUMN_INITIAL_VALUE, 0.0)
                                        values.put(Constant.COLUMN_INITIAL_DISTANCE, 0.0)
                                    }
                                }
                                insertColumnValue(cookie, values)
                            } else {
                                updateColumnValue(cookie)
                            }
                        } else {
                            val errStringRes = when (token) {
                                TOKEN_UPDATE_INITIAL_VALUE -> R.string.set_initial_value_failed
                                TOKEN_UPDATE_INITIAL_DISTANCE -> R.string.set_initial_distance_failed
                                else -> 0
                            }
                            if (errStringRes != 0) {
                                SimpleCustomizeToast.show(errStringRes)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDeleteComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        when (token) {
            TOKEN_DELETE_WARNER_TYPE -> {
                if (affectedRowCount > 0) {
                    if (cookie is Bundle) {
                        when (cookie.getInt(ARGUMENT_KEY_TOKEN)) {
                            TOKEN_INSERT_WARNER_TYPE -> {
                                insertWarnerType(cookie)
                            }
                        }
                    } else {
                        refreshSensorConfig()
                    }
                } else {
                    SimpleCustomizeToast.show(R.string.warner_type_modify_failed)
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
        if (rowId != -1L) {
            if (token == TOKEN_ADD_MEASUREMENT_CONFIG
                    && cookie is Bundle) {
                val innerToken = cookie.getInt(ARGUMENT_KEY_TOKEN)
                if (innerToken == TOKEN_INSERT_WARNER_TYPE) {
                    databaseHandler.startQuery(TOKEN_QUERY_MEASUREMENT_CONFIG_ID,
                            cookie, false, Constant.TABLE_MEASUREMENT_CONFIGURATION,
                            arrayOf(Constant.COLUMN_COMMON_ID),
                            "${Constant.COLUMN_SENSOR_CONFIGURATION_ID} = ? AND ${Constant.COLUMN_MEASUREMENT_VALUE_ID} = ?",
                            arrayOf(getSensorConfigId().toString(), cookie.getLong(ARGUMENT_KEY_MEASUREMENT_ID).toString()),
                            null, null, null, null)
                } else {
                    queryFunctionId(cookie)
                }
            } else {
                refreshSensorConfig()
            }
        } else {
            val errStringRes = when (token) {
                TOKEN_ADD_MEASUREMENT_CONFIG -> R.string.measurement_custom_name_modify_failed
                TOKEN_INSERT_WARNER_TYPE -> R.string.warner_type_modify_failed
                TOKEN_UPDATE_HIGH_LIMIT -> R.string.set_high_limit_failed
                TOKEN_UPDATE_LOW_LIMIT -> R.string.set_low_limit_failed
                TOKEN_UPDATE_ABNORMAL_VALUE -> R.string.set_abnormal_value_failed
                TOKEN_UPDATE_INITIAL_VALUE -> R.string.set_initial_value_failed
                TOKEN_UPDATE_INITIAL_DISTANCE -> R.string.set_initial_distance_failed
                else -> 0
            }
            if (errStringRes != 0) {
                SimpleCustomizeToast.show(errStringRes)
            }
        }
    }

    private fun queryFunctionId(cookie: Bundle) {
        val builder = StringBuilder()
        builder.append("SELECT ").append("m.")
                .append(Constant.COLUMN_COMMON_ID)
                .append(' ').append(Constant.COLUMN_MEASUREMENT_CONFIGURATION_ID)
                .append(",f.").append(Constant.COLUMN_COMMON_ID)
                .append(' ').append(Constant.COLUMN_FUNCTION_ID)
                .append(" FROM ").append(Constant.TABLE_MEASUREMENT_CONFIGURATION)
                .append(" m LEFT JOIN ").append(cookie.getString(ARGUMENT_KEY_TABLE_NAME))
                .append(" f ON m.").append(Constant.COLUMN_COMMON_ID)
                .append(" = f.").append(Constant.COLUMN_COMMON_ID)
                .append(" WHERE ").append(Constant.COLUMN_SENSOR_CONFIGURATION_ID)
                .append(" = ").append(getSensorConfigId())
                .append(" AND ").append(Constant.COLUMN_MEASUREMENT_VALUE_ID)
                .append(" = ").append(cookie.getLong(ARGUMENT_KEY_MEASUREMENT_ID))
        databaseHandler.startRawQuery(TOKEN_QUERY_MEASUREMENT_CONFIG_ID,
                cookie, builder.toString(), null)
    }

    override fun onUpdateComplete(token: Int, cookie: Any?, affectedRowCount: Int) {
        if (affectedRowCount > 0) {
            refreshSensorConfig()
        } else {
            val errStringRes = when (token) {
                TOKEN_UPDATE_SENSOR_CUSTOM_NAME ->  R.string.sensor_custom_name_modify_failed
                TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME -> R.string.measurement_custom_name_modify_failed
                //TOKEN_UPDATE_HIGH_LIMIT -> R.string.set_high_limit_failed
                //TOKEN_UPDATE_LOW_LIMIT -> R.string.set_low_limit_failed
                //TOKEN_UPDATE_ABNORMAL_VALUE -> R.string.set_abnormal_value_failed
                TOKEN_UPDATE_INITIAL_VALUE -> R.string.set_initial_value_failed
                TOKEN_UPDATE_INITIAL_DISTANCE -> R.string.set_initial_distance_failed
                else -> 0
            }
            if (errStringRes != 0) {
                SimpleCustomizeToast.show(errStringRes)
            }
        }
//        when (token) {
//            TOKEN_UPDATE_SENSOR_CUSTOM_NAME -> {
//                if (affectedRowCount > 0) {
//                    refreshSensorConfig()
//                } else {
//                    SimpleCustomizeToast.show(R.string.sensor_custom_name_modify_failed)
//                }
//            }
//            TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME -> {
//                if (affectedRowCount > 0) {
//                    refreshSensorConfig()
//                } else {
//                    SimpleCustomizeToast.show(R.string.measurement_custom_name_modify_failed)
//                }
//            }
//            TOKEN_UPDATE_HIGH_LIMIT
//        }
    }

    private fun refreshSensorConfig() {
        supportLoaderManager.getLoader<SensorConfiguration>(0)?.onContentChanged()
    }
}
