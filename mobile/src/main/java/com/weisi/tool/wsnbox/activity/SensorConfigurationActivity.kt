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
import com.weisi.tool.wsnbox.bean.corrector.Binarization
import com.weisi.tool.wsnbox.bean.corrector.CorrectorUtil
import com.weisi.tool.wsnbox.bean.corrector.LinearFittingCorrector
import com.weisi.tool.wsnbox.bean.decorator.CommonMeasurementDecorator
import com.weisi.tool.wsnbox.io.Constant
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.processor.loader.SensorConfigurationLoader
import com.weisi.tool.wsnbox.util.NullHelper
import kotlinx.android.synthetic.main.activity_sensor_configuration.*
import kotlinx.android.synthetic.main.lh_sensor_base_config.view.*

class SensorConfigurationActivity : BaseActivity(),
        LoaderManager.LoaderCallbacks<SensorConfiguration>,
        EditDialog.OnContentReceiver,
        ListDialog.OnItemSelectedListener,
        SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener {

    private val TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME1 = 1
    private val TOKEN_UPDATE_MEASUREMENT_CUSTOM_UNIT = 2
    private val TOKEN_UPDATE_MEASUREMENT_DECIMALS = 3
    private val TOKEN_ADD_MEASUREMENT_CONFIG = 4
    private val TOKEN_DELETE_WARNER_TYPE = 5
    private val TOKEN_INSERT_WARNER_TYPE = 6
    private val TOKEN_QUERY_MEASUREMENT_CONFIG_ID = 7
    private val TOKEN_UPDATE_SENSOR_CUSTOM_NAME = 8
    private val TOKEN_UPDATE_HIGH_LIMIT = 9
    private val TOKEN_UPDATE_LOW_LIMIT = 10
    private val TOKEN_UPDATE_ABNORMAL_VALUE = 11
    private val TOKEN_UPDATE_INITIAL_VALUE = 12
    private val TOKEN_UPDATE_INITIAL_DISTANCE = 13
    private val TOKEN_UPDATE_CORRECTOR_TYPE = 14
    private val TOKEN_INSERT_LINEAR_FITTING_CORRECTOR_ITEM_GROUP = 15
    private val TOKEN_INSERT_LINEAR_FITTING_CORRECTOR = 16
    private val TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_CORRECTED_VALUE = 17
    private val TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_SAMPLING_VALUE = 18
    private val TOKEN_DELETE_LINEAR_FITTING_CORRECTOR_ITEM_GROUP = 19

    private val DIALOG_TAG_MODIFY_SENSOR_CUSTOM_NAME = "mdf_sns_cus_name"
    private val DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME = "mdf_msm_cus_name"
    private val DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_UNIT = "mdf_msm_cus_unit"
    private val DIALOG_TAG_MODIFY_MEASUREMENT_DECIMALS = "mdf_msm_decimals"
    private val DIALOG_TAG_CHOOSE_WARNER_TYPE = "mdf_warner_type"
    private val DIALOG_TAG_CHOOSE_CORRECTOR_TYPE = "mdf_corrector_type"
    private val DIALOG_TAG_MODIFY_VALUE = "mdf_warner_value"
    private val DIALOG_TAG_MODIFY_CORRECTED_VALUE = "mdf_corrected_value"
    private val DIALOG_TAG_MODIFY_SAMPLING_VALUE = "mdf_sample_value"
    private val DIALOG_TAG_CHOOSE_LINEAR_FITTING_CORRECTOR_MODIFY_TYPE = "mdf_lfc_mdf_type"
    private val DIALOG_TAG_INPUT_VALUE_EMPTY = "input_value_empty"
    private val DIALOG_TAG_INPUT_VALUE_ERROR = "input_value_err"

    private val ARGUMENT_KEY_ITEM_POSITION = "position"
    private val ARGUMENT_KEY_TOKEN = "token"
    private val ARGUMENT_KEY_MEASUREMENT_CONFIG_ID = "msm_cfg_id"
    private val ARGUMENT_KEY_MEASUREMENT_ID = "msm_id"
    //private val ARGUMENT_KEY_MEASUREMENT_TYPE = "msm_type"
    private val ARGUEMENT_KEY_WARNER_TYPE = "warner_type"
    private val ARGUMENT_KEY_COLUMN_NAME = "col_name"
    private val ARGUMENT_KEY_COLUMN_VALUE = "col_value"
    private val ARGUMENT_KEY_TABLE_NAME = "tb_name"
    //private val ARGUMENT_KEY_DECIMALS = "msm_decimals"
    private val ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION = "lfc_pos"
    private val ARGUMENT_KEY_CORRECTED_VALUE = "corrected_v"
    private val ARGUMENT_KEY_SAMPLING_VALUE = "sample_v"
    private val ARGUMENT_KEY_NEED_INSERT = "need_inst"

    private val adapter = SensorConfigAdapter()
    private lateinit var vBaseInfo: View
    private val databaseHandler = SensorDatabase.buildAsyncEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_configuration)

        rv_sensor_config.layoutManager = LinearLayoutManager(this)
        rv_sensor_config.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(rv_sensor_config) {
            override fun onItemClick(v: View?, position: Int) {
                val measure = adapter.getItemByPosition(position)
                when (v?.id) {
                    R.id.tv_custom_name_value -> {
                        val dialog = EditDialog()
                        dialog.setTitle(R.string.modify_measurement_custom_name)
                        dialog.setContent(measure.configuration.decorator?.decorateName(null))
                        //putDialogArgumentsForMeasure(dialog, measure)
                        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME)
                    }
                    R.id.tv_custom_unit_value -> {
                        val dialog = EditDialog()
                        dialog.setTitle(R.string.modify_measurement_custom_unit)
                        val decorator = measure.configuration.decorator
                        dialog.setContent(if (decorator is CommonMeasurementDecorator) {
                            decorator.customUnit
                        } else {
                            ""
                        })
                        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_UNIT)
                    }
                    R.id.tv_decimals_value -> {
                        val dialog = EditDialog()
                        dialog.setTitle(R.string.modify_measurement_decimals)
                        val decorator = measure.configuration.decorator
                        dialog.setContent(if (decorator is CommonMeasurementDecorator) {
                            decorator.getOriginDecimalsLabel()
                        } else {
                            ""
                        })
                        dialog.setSummary(R.string.no_effect_for_modify_only_decimals)
                        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_MEASUREMENT_DECIMALS)
                    }
                    R.id.tv_warner_type_value -> {
                        val dialog = ListDialog()
                        dialog.setTitle(R.string.choose_warner_type)
                        dialog.setItems(resources.getStringArray(R.array.warner_types))
                        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                        //putDialogArgumentsForMeasure(dialog, measure)
                        dialog.show(supportFragmentManager, DIALOG_TAG_CHOOSE_WARNER_TYPE)
                    }
                    R.id.tv_corrector_type_value -> {
                        val dialog = ListDialog()
                        dialog.setTitle(R.string.choose_corrector_type)
                        dialog.setItems(resources.getStringArray(R.array.corrector_types))
                        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                        dialog.show(supportFragmentManager, DIALOG_TAG_CHOOSE_CORRECTOR_TYPE)
                    }
                    R.id.tv_high_limit_value -> {
                        showValueSetDialog(v,
                                R.string.set_high_limit,
                                position,
                                TOKEN_UPDATE_HIGH_LIMIT,
                                Constant.TABLE_GENERAL_SINGLE_RANGE_WARNER,
                                Constant.COLUMN_HIGH_LIMIT)
                    }
                    R.id.tv_low_limit_value -> {
                        showValueSetDialog(v,
                                R.string.set_low_limit,
                                position,
                                TOKEN_UPDATE_LOW_LIMIT,
                                Constant.TABLE_GENERAL_SINGLE_RANGE_WARNER,
                                Constant.COLUMN_LOW_LIMIT)
                    }
                    R.id.tv_abnormal_value -> {
                        showValueSetDialog(v,
                                R.string.set_abnormal_value,
                                position,
                                TOKEN_UPDATE_ABNORMAL_VALUE,
                                Constant.TABLE_GENERAL_SWITCH_WARNER,
                                Constant.COLUMN_ABNORMAL_VALUE)
                    }
                    R.id.tv_initial_value_value -> {
                        showValueSetDialog(v,
                                R.string.set_initial_value,
                                position,
                                TOKEN_UPDATE_INITIAL_VALUE,
                                Constant.TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION,
                                Constant.COLUMN_INITIAL_VALUE)
                    }
                    R.id.tv_initial_distance_value -> {
                        showValueSetDialog(v,
                                R.string.set_initial_distance,
                                position,
                                TOKEN_UPDATE_INITIAL_DISTANCE,
                                Constant.TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION,
                                Constant.COLUMN_INITIAL_DISTANCE)
                    }
                }
                val tag = v?.tag
                if (tag is String) {
                    when {
                        tag.contains('c') -> {
                            val groupPosition = tag.substring(2).toInt()
                            val corrector = measure.configuration.corrector as? LinearFittingCorrector
                            val needInsert = NullHelper.ifNullOrNot(corrector, { true }, {
                                groupPosition >= it.groupCount()
                            })
                            showUpdateCorrectedValueDialog(corrector,
                                    groupPosition,
                                    position,
                                    if (needInsert) {
                                        null
                                    } else {
                                        corrector?.getSamplingValue(groupPosition)
                                    },
                                    needInsert)
                        }
                        tag.contains('s') -> {
                            val groupPosition = tag.substring(2).toInt()
                            val corrector = measure.configuration.corrector as? LinearFittingCorrector
                            val needInsert = NullHelper.ifNullOrNot(corrector, { true }, {
                                groupPosition >= it.groupCount()
                            })
                            showUpdateSamplingValueDialog(corrector,
                                    groupPosition,
                                    position,
                                    if (needInsert) {
                                        null
                                    } else {
                                        corrector?.getCorrectedValue(groupPosition)
                                    },
                                    needInsert)
                        }
                    }
                }
            }

            override fun onItemLongClick(v: View?, position: Int) {
                val tag = v?.tag
                if (tag is String && (tag.contains('s') || tag.contains('c'))) {
                    val groupPosition = tag.substring(2).toInt()
                    val dialog = ListDialog()
                    dialog.setTitle(R.string.select_linear_fitting_corrector_modify_type)
                    dialog.setItems(resources.getStringArray(R.array.linear_fitting_corrector_modify_type))
                    dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
                    dialog.arguments?.putInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION, groupPosition)
                    dialog.show(supportFragmentManager, DIALOG_TAG_CHOOSE_LINEAR_FITTING_CORRECTOR_MODIFY_TYPE)
                }
            }
        }.setMinRangeEnable(true)
                .setIsLongPressEnabled(true))

        val wrapper = HeaderAndFooterWrapper(adapter)
        val inflater = LayoutInflater.from(this)
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_sensor_config, R.string.basic_info))
        wrapper.addHeaderView(getBaseInfoView(inflater))
        wrapper.addHeaderView(getGroupLabelView(inflater, rv_sensor_config, R.string.measurement_list))
        rv_sensor_config.adapter = wrapper

        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    private fun showUpdateCorrectedValueDialog(corrector: LinearFittingCorrector?,
                                               groupPosition: Int,
                                               itemPosition: Int,
                                               samplingValue: Float?,
                                               needInsert: Boolean) {
        //val corrector = measure.configuration.corrector as? LinearFittingCorrector
        val dialog = EditDialog()
        dialog.setTitle(R.string.set_corrected_value)
        dialog.setContent(if (needInsert) {
            ""
        } else {
            corrector?.getFormatCorrectedValue(groupPosition)
        })
        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, itemPosition)
        dialog.arguments?.putInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION, groupPosition)
        samplingValue?.let {
            dialog.arguments?.putFloat(ARGUMENT_KEY_SAMPLING_VALUE, it)
        }
        dialog.arguments?.putBoolean(ARGUMENT_KEY_NEED_INSERT, needInsert)
        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_CORRECTED_VALUE)
    }

    private fun showUpdateSamplingValueDialog(corrector: LinearFittingCorrector?,
                                              groupPosition: Int,
                                              position: Int,
                                              correctedValue: Float?,
                                              needInsert: Boolean) {
        //val corrector = measure.configuration.corrector as? LinearFittingCorrector
        val dialog = EditDialog()
        dialog.setTitle(R.string.set_sampling_value)
        dialog.setContent(if (needInsert) {
            ""
        } else {
            corrector?.getFormatSamplingValue(groupPosition)
        })
        dialog.arguments?.putInt(ARGUMENT_KEY_ITEM_POSITION, position)
        dialog.arguments?.putInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION, groupPosition)
        correctedValue?.let {
            dialog.arguments?.putFloat(ARGUMENT_KEY_CORRECTED_VALUE, it)
        }
        dialog.arguments?.putBoolean(ARGUMENT_KEY_NEED_INSERT, needInsert)
        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_SAMPLING_VALUE)
    }

    private fun showValueSetDialog(view: View,
                                   titleStringRes: Int,
                                   itemPosition: Int,
                                   token: Int,
                                   tableName: String,
                                   columnName: String) {
        val dialog = EditDialog()
        dialog.setTitle(titleStringRes)
        dialog.setContent((view as? TextView)?.text?.toString())
        dialog.arguments?.let { arguments ->
            arguments.putInt(ARGUMENT_KEY_ITEM_POSITION, itemPosition)
            arguments.putInt(ARGUMENT_KEY_TOKEN, token)
            arguments.putString(ARGUMENT_KEY_TABLE_NAME, tableName)
            arguments.putString(ARGUMENT_KEY_COLUMN_NAME, columnName)
        }
//        putDialogArgumentsForValueUpdate(dialog,
//                measure.configurationId,
//                measure.id,
//                token,
//                tableName,
//                columnName)
        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_VALUE)
    }

//    private fun showValueSetDialog(titleStringRes: Int,
//                                   view: View,
//                                   measure: SensorConfiguration.Measure,
//                                   token: Int,
//                                   tableName: String,
//                                   columnName: String) {
//        val dialog = EditDialog()
//        dialog.setTitle(titleStringRes)
//        dialog.setContent((view as? TextView)?.text?.toString())
//        putDialogArgumentsForValueUpdate(dialog,
//                measure.configurationId,
//                measure.id,
//                token,
//                tableName,
//                columnName)
//        dialog.show(supportFragmentManager, DIALOG_TAG_MODIFY_VALUE)
//    }

//    private fun putDialogArgumentsForMeasure(dialog: BaseServiceDialog<*>, measure: SensorConfiguration.Measure) {
//        dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measure.configurationId)
//        dialog.arguments?.putInt(ARGUMENT_KEY_MEASUREMENT_TYPE, measure.type)
//        if (measure.configurationId == 0L) {
//            dialog.arguments?.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measure.id)
//        }
//    }

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

    override fun onReceive(dialog: EditDialog, oldValue: String?, newValue: String?): Boolean {
        if (TextUtils.equals(oldValue, newValue)) {
            return true
        }
        when (dialog.tag) {
            DIALOG_TAG_MODIFY_SENSOR_CUSTOM_NAME -> {
                val values = ContentValues()
                values.put(Constant.COLUMN_CUSTOM_NAME, newValue)
                databaseHandler.startUpdate(TOKEN_UPDATE_SENSOR_CUSTOM_NAME,
                    null, Constant.TABLE_SENSOR_CONFIGURATION,
                    values, "${Constant.COLUMN_COMMON_ID} = ?",
                    arrayOf(getSensorConfigId().toString()), SQLiteDatabase.CONFLICT_NONE)
            }
            DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_NAME -> {
                val position = dialog.arguments?.getInt(ARGUMENT_KEY_ITEM_POSITION) ?: return true
                updateMeasurementCustomName(adapter.getItemByPosition(position), newValue)
//                val measurementConfigId = getMeasurementConfigId(dialog)
//                if (measurementConfigId == 0L) {
//                    addMeasurementConfigWithCustomName(dialog, newValue)
//                } else {
//                    val values = ContentValues()
//                    values.put(Constant.COLUMN_CUSTOM_NAME, newValue)
//                    databaseHandler.startUpdate(TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME,
//                            null, Constant.TABLE_MEASUREMENT_CONFIGURATION,
//                            values, "${Constant.COLUMN_COMMON_ID} = ?",
//                            arrayOf(getMeasurementConfigId(dialog).toString()),
//                    SQLiteDatabase.CONFLICT_NONE)
//                }
            }
            DIALOG_TAG_MODIFY_MEASUREMENT_CUSTOM_UNIT -> {
                val position = dialog.arguments?.getInt(ARGUMENT_KEY_ITEM_POSITION) ?: return true
                updateMeasurementCustomUnit(adapter.getItemByPosition(position), newValue)
            }
            DIALOG_TAG_MODIFY_MEASUREMENT_DECIMALS -> {
                return NullHelper.ifNullOrNot(newValue?.toIntOrNull(), {
                    showInputErrorDialog()
                }, { decimals ->
                    val position = dialog.arguments?.getInt(ARGUMENT_KEY_ITEM_POSITION) ?: return@ifNullOrNot true
                    updateMeasurementDecimals(adapter.getItemByPosition(position), decimals)
                    true
                })
            }
            DIALOG_TAG_MODIFY_VALUE -> {
                if (newValue.isNullOrEmpty()) {
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle(R.string.input_value_empty)
                    confirmDialog.setDrawCancelButton(false)
                    confirmDialog.show(supportFragmentManager, DIALOG_TAG_INPUT_VALUE_EMPTY)
                    return false
                }
                val value = newValue.toDoubleOrNull()
                if (value === null) {
                    return showInputErrorDialog()
                }
                val arguments = dialog.arguments ?: return true
                val measure = adapter.getItemByPosition(arguments.getInt(ARGUMENT_KEY_ITEM_POSITION))
                val args = Bundle(arguments)
                args.putDouble(ARGUMENT_KEY_COLUMN_VALUE, value)
                if (measure.configurationId == 0L) {
                    addMeasurementConfig(adapter.getItemByPosition(arguments.getInt(ARGUMENT_KEY_ITEM_POSITION)), args)
                    //replaceMeasurementConfig(adapter.getItemByPosition(arguments.getInt(ARGUMENT_KEY_ITEM_POSITION)), ContentValues(), TOKEN_ADD_MEASUREMENT_CONFIG, args)
                    //addMeasurementConfig(dialog, args)
                } else {
                    args.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measure.configurationId)
                    args.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measure.id)
                    queryFunctionId(args)
                }
            }
            DIALOG_TAG_MODIFY_CORRECTED_VALUE -> {
                return NullHelper.ifNullOrNot(newValue?.toFloatOrNull(), {
                    showInputErrorDialog()
                }, { correctedValue ->
                    val arguments = dialog.arguments ?: return@ifNullOrNot true
                    val position = arguments.getInt(ARGUMENT_KEY_ITEM_POSITION)
                    val measure = adapter.getItemByPosition(position)
                    val corrector = measure.configuration.corrector
                    val groupPosition = arguments.getInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION)
                    val needInsert = arguments.getBoolean(ARGUMENT_KEY_NEED_INSERT)
                    if (corrector is LinearFittingCorrector) {
                        if (arguments.containsKey(ARGUMENT_KEY_SAMPLING_VALUE)) {
                            if (needInsert) {
                                insertLinearFittingCorrectorItemGroup(measure, correctedValue, arguments.getFloat(ARGUMENT_KEY_SAMPLING_VALUE), groupPosition)
                            } else {
                                updateLinearFittingCorrectorCorrectedValue(measure, correctedValue, groupPosition)
                            }
                        } else {
                            showUpdateSamplingValueDialog(corrector, groupPosition, position, correctedValue, needInsert)
                        }
                    } else {
                        if (arguments.containsKey(ARGUMENT_KEY_SAMPLING_VALUE)) {
                            insertLinearFittingCorrector(measure, correctedValue, arguments.getFloat(ARGUMENT_KEY_SAMPLING_VALUE))
                        } else {
                            showUpdateSamplingValueDialog(null, groupPosition, position, correctedValue, needInsert)
                        }
                    }
                    true
                })
            }
            DIALOG_TAG_MODIFY_SAMPLING_VALUE -> {
                NullHelper.ifNullOrNot(newValue?.toFloatOrNull(), {
                    showInputErrorDialog()
                }, { samplingValue ->
                    val arguments = dialog.arguments ?: return@ifNullOrNot true
                    val position = arguments.getInt(ARGUMENT_KEY_ITEM_POSITION)
                    val measure = adapter.getItemByPosition(position)
                    val corrector = measure.configuration.corrector
                    val groupPosition = arguments.getInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION)
                    val needInsert = arguments.getBoolean(ARGUMENT_KEY_NEED_INSERT)
                    if (corrector is LinearFittingCorrector) {
                        if (arguments.containsKey(ARGUMENT_KEY_CORRECTED_VALUE)) {
                            if (needInsert) {
                                insertLinearFittingCorrectorItemGroup(measure, arguments.getFloat(ARGUMENT_KEY_CORRECTED_VALUE), samplingValue, groupPosition)
                            } else {
                                updateLinearFittingCorrectorSamplingValue(measure, samplingValue, groupPosition)
                            }
                        } else {
                            showUpdateSamplingValueDialog(corrector, groupPosition, position, samplingValue, needInsert)
                        }
                    } else {
                        if (arguments.containsKey(ARGUMENT_KEY_CORRECTED_VALUE)) {
                            insertLinearFittingCorrector(measure, arguments.getFloat(ARGUMENT_KEY_CORRECTED_VALUE), samplingValue)
                        } else {
                            showUpdateSamplingValueDialog(null, groupPosition, position, samplingValue, needInsert)
                        }
                    }
                    true
                })
            }
        }
        return true
    }

    private fun showInputErrorDialog(): Boolean {
        val confirmDialog = ConfirmDialog()
        confirmDialog.setTitle(R.string.input_value_parse_failed)
        confirmDialog.setDrawCancelButton(false)
        confirmDialog.show(supportFragmentManager, DIALOG_TAG_INPUT_VALUE_ERROR)
        return false
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

//    private fun addMeasurementConfigWithCustomName(dialog: BaseServiceDialog<*>, customName: String?) {
//        return addMeasurementConfig(dialog, null, customName)
//    }
//
//    private fun addMeasurementConfig(dialog: BaseServiceDialog<*>, args: Bundle?) {
//        return addMeasurementConfig(dialog, args, null)
//    }
//
//    private fun addMeasurementConfig(dialog: BaseServiceDialog<*>, args: Bundle?, customName: String?) {
//        return addMeasurementConfig(getMeasurementId(dialog), getMeasurementType(dialog), args, customName)
//    }
//
//    private fun addMeasurementConfig(measurementId: Long, measurementType: Int, args: Bundle?, customName: String?) {
//        val values = ContentValues()
//        values.put(Constant.COLUMN_SENSOR_CONFIGURATION_ID, getSensorConfigId())
//        values.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, measurementId)
//        val type = SensorConfiguration.Measure.getConfigType(measurementType)
//        if (type != SensorConfiguration.Measure.CT_NORMAL) {
//            values.put(Constant.COLUMN_TYPE, type)
//        }
//        if (args === null) {
//            values.put(Constant.COLUMN_CUSTOM_NAME, customName)
//        }
//        databaseHandler.startInsert(TOKEN_ADD_MEASUREMENT_CONFIG,
//                args, Constant.TABLE_MEASUREMENT_CONFIGURATION,
//                values, SQLiteDatabase.CONFLICT_NONE)
//    }

    private fun updateMeasurementCustomName(measure: SensorConfiguration.Measure,
                                            customName: String?) {
        val values = ContentValues()
        values.put(Constant.COLUMN_CUSTOM_NAME, customName)
        replaceMeasurementConfig(measure, values, TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME1, null)
    }

    private fun updateMeasurementCustomUnit(measure: SensorConfiguration.Measure,
                                            customUnit: String?) {
        val values = ContentValues()
        values.put(Constant.COLUMN_CUSTOM_UNIT, customUnit)
        if (!customUnit.isNullOrEmpty()) {
            val decorator = measure.configuration.decorator
            if (decorator is CommonMeasurementDecorator
                    && decorator.getOriginDecimals() == -1) {
                values.put(Constant.COLUMN_DECIMALS, 3)
            }
        }
        replaceMeasurementConfig(measure, values, TOKEN_UPDATE_MEASUREMENT_CUSTOM_UNIT, null)
    }

    private fun updateMeasurementDecimals(measure: SensorConfiguration.Measure,
                                          decimals: Int) {
        val values = ContentValues()
        values.put(Constant.COLUMN_DECIMALS, decimals)
        replaceMeasurementConfig(measure, values, TOKEN_UPDATE_MEASUREMENT_DECIMALS, null)
    }

    private fun updateWarnerType(measure: SensorConfiguration.Measure, warnerType: Int) {
        val oldWarnerType = measure.getWarnerType()
        val newWarnerType = warnerType
        if (newWarnerType == oldWarnerType) {
            return
        }
        if (oldWarnerType != SensorConfiguration.Measure.WT_NONE) {
            if (measure.configurationId != 0L) {
                val args = if (newWarnerType != SensorConfiguration.Measure.WT_NONE) {
                    val bundle = Bundle()
                    bundle.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
                    bundle.putLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, measure.configurationId)
                    bundle.putInt(ARGUEMENT_KEY_WARNER_TYPE, newWarnerType)
                    bundle
                } else {
                    null
                }
                databaseHandler.startDelete(TOKEN_DELETE_WARNER_TYPE,
                        args, getWarnerTableName(oldWarnerType),
                        "${Constant.COLUMN_COMMON_ID} = ?",
                        arrayOf(measure.configurationId.toString()))
            } else {
                throw IllegalArgumentException("warner type exist while measurement does not")
            }
        } else {
            if (newWarnerType != SensorConfiguration.Measure.WT_NONE) {
                insertWarnerType(measure, warnerType)
            }
        }
    }

    private fun updateCorrectorType(measure: SensorConfiguration.Measure, correctorType: Int) {
        val oldCorrectorType = measure.getCorrectorType()
        if (oldCorrectorType == correctorType) {
            return
        }
        val values = ContentValues()
        values.put(Constant.COLUMN_CORRECTOR, CorrectorUtil.toByteArray(CorrectorUtil.buildCorrector(correctorType)))
        replaceMeasurementConfig(measure, values, TOKEN_UPDATE_CORRECTOR_TYPE, null)
    }

//    private fun updateLinearFittingCorrectorGroup(measure: SensorConfiguration.Measure, groupPosition: Int, correctedValue: Float?, samplingValue: Float?) {
//
//        val corrector = measure.configuration.corrector
//        val values = ContentValues()
//        if (corrector is LinearFittingCorrector) {
//            if (corrector.groupCount() == 0) {
//                insertLinearFittingCorrector(measure, correctedValue, samplingValue, values)
//            } else {
//                if (groupPosition < corrector.groupCount()) {
//                    correctedValue?.let {
//
//                    }
//                }
//            }
//        } else {
//            insertLinearFittingCorrector(measure, correctedValue, samplingValue, values)
//        }
//    }

    private fun insertLinearFittingCorrector(measure: SensorConfiguration.Measure, correctedValue: Float, samplingValue: Float) {
        //关于单位暂时这么做
        val correctedValueUnit = if (ID.getDataTypeAbsValue(measure.id) == 0x60) {
            "kN"
        } else {
            ""
        }
        val samplingValueUnit = ""
        replaceMeasurementCorrector(measure, LinearFittingCorrector(FloatArray(1) { correctedValue },
                FloatArray(1) { samplingValue },
                correctedValueUnit,
                samplingValueUnit),
                TOKEN_INSERT_LINEAR_FITTING_CORRECTOR)
        //replaceMeasurementConfig(measure, values, TOKEN_INSERT_LINEAR_FITTING_CORRECTOR, null)
    }

    private fun insertLinearFittingCorrectorItemGroup(measure: SensorConfiguration.Measure, correctedValue: Float, samplingValue: Float, groupPosition: Int) {
        val srcCorrector = measure.configuration.corrector as? LinearFittingCorrector ?: throw IllegalArgumentException("current corrector is not LinearFittingCorrector")
        val srcGroupCount = srcCorrector.groupCount()
        val dstCorrector = LinearFittingCorrector(FloatArray(srcGroupCount + 1) { i ->
            when {
                i < groupPosition -> srcCorrector.getCorrectedValue(i)
                i > groupPosition -> srcCorrector.getCorrectedValue(i - 1)
                else -> correctedValue
            }
        }, FloatArray(srcGroupCount + 1) { i ->
            when {
                i < groupPosition -> srcCorrector.getSamplingValue(i)
                i > groupPosition -> srcCorrector.getSamplingValue(i - 1)
                else -> samplingValue
            }
        }, srcCorrector.correctedValueUnit,
                srcCorrector.samplingValueUnit)
        replaceMeasurementCorrector(measure, dstCorrector, TOKEN_INSERT_LINEAR_FITTING_CORRECTOR_ITEM_GROUP)
    }

    private fun updateLinearFittingCorrectorCorrectedValue(measure: SensorConfiguration.Measure, correctedValue: Float, groupPosition: Int) {
        val srcCorrector = measure.configuration.corrector as? LinearFittingCorrector ?: throw IllegalArgumentException("current corrector is not LinearFittingCorrector")
        val srcGroupCount = srcCorrector.groupCount()
        if (groupPosition >= srcGroupCount) {
            throw IllegalArgumentException("current LinearFittingCorrector no such corrected value")
        }
        val dstCorrector = LinearFittingCorrector(FloatArray(srcGroupCount) { i ->
            if (i == groupPosition) {
                correctedValue
            } else {
                srcCorrector.getCorrectedValue(i)
            }
        }, FloatArray(srcGroupCount) { i ->
            srcCorrector.getSamplingValue(i)
        }, srcCorrector.correctedValueUnit,
                srcCorrector.samplingValueUnit)
        replaceMeasurementCorrector(measure, dstCorrector, TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_CORRECTED_VALUE)
    }

    private fun updateLinearFittingCorrectorSamplingValue(measure: SensorConfiguration.Measure, samplingValue: Float, groupPosition: Int) {
        val srcCorrector = measure.configuration.corrector as? LinearFittingCorrector ?: throw IllegalArgumentException("current corrector is not LinearFittingCorrector")
        val srcGroupCount = srcCorrector.groupCount()
        if (groupPosition >= srcGroupCount) {
            throw IllegalArgumentException("current LinearFittingCorrector no such sampling value")
        }
        val dstCorrector = LinearFittingCorrector(FloatArray(srcGroupCount) { i ->
            srcCorrector.getCorrectedValue(i)
        }, FloatArray(srcGroupCount) { i ->
            if (i == groupPosition) {
                samplingValue
            } else {
                srcCorrector.getSamplingValue(i)
            }
        }, srcCorrector.correctedValueUnit,
                srcCorrector.samplingValueUnit)
        replaceMeasurementCorrector(measure, dstCorrector, TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_SAMPLING_VALUE)
    }

    private fun deleteLinearFittingCorrectorItemGroup(measure: SensorConfiguration.Measure, groupPosition: Int) {
        val srcCorrector = measure.configuration.corrector as? LinearFittingCorrector ?: throw IllegalArgumentException("current corrector is not LinearFittingCorrector")
        val srcGroupCount = srcCorrector.groupCount()
        if (groupPosition >= srcGroupCount) {
            throw IllegalArgumentException("current LinearFittingCorrector no such sampling value")
        }
        val dstCorrector = LinearFittingCorrector(FloatArray(srcGroupCount - 1) { i ->
            if (i < groupPosition) {
                srcCorrector.getCorrectedValue(i)
            } else {
                srcCorrector.getCorrectedValue(i + 1)
            }
        }, FloatArray(srcGroupCount - 1) { i ->
            if (i < groupPosition) {
                srcCorrector.getSamplingValue(i)
            } else {
                srcCorrector.getSamplingValue(i + 1)
            }
        }, srcCorrector.correctedValueUnit,
                srcCorrector.samplingValueUnit)
        replaceMeasurementCorrector(measure, dstCorrector, TOKEN_DELETE_LINEAR_FITTING_CORRECTOR_ITEM_GROUP)
    }

    private fun replaceMeasurementCorrector(measure: SensorConfiguration.Measure, corrector: Binarization, token: Int) {
        val values = ContentValues()
        values.put(Constant.COLUMN_CORRECTOR,
                corrector.toByteArray())
        replaceMeasurementConfig(measure, values, token, null)
    }

    private fun replaceMeasurementConfig(measure: SensorConfiguration.Measure,
                                         values: ContentValues,
                                         token: Int,
                                         args: Bundle?) {
        if (measure.configurationId == 0L) {
            //新增测量量配置
            addMeasurementConfig(measure, values, token, args)
        } else {
            //更新测量量配置
            databaseHandler.startUpdate(token,
                    null, Constant.TABLE_MEASUREMENT_CONFIGURATION,
                    values, "${Constant.COLUMN_COMMON_ID} = ${measure.configurationId}",
                    null,
            SQLiteDatabase.CONFLICT_NONE)
        }
    }

    private fun addMeasurementConfig(measure: SensorConfiguration.Measure, args: Bundle?) {
        addMeasurementConfig(measure, ContentValues(), TOKEN_ADD_MEASUREMENT_CONFIG, null)
    }

    private fun addMeasurementConfig(measure: SensorConfiguration.Measure, values: ContentValues, token: Int, args: Bundle?) {
        values.put(Constant.COLUMN_SENSOR_CONFIGURATION_ID, getSensorConfigId())
        values.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, measure.id)
        values.put(Constant.COLUMN_TYPE, measure.getConfigType())
        databaseHandler.startInsert(token,
                args, Constant.TABLE_MEASUREMENT_CONFIGURATION,
                values, SQLiteDatabase.CONFLICT_NONE)
    }

    override fun onItemSelected(dialog: ListDialog, position: Int, items: Array<out Any>) {
        when (dialog.tag) {
            DIALOG_TAG_CHOOSE_WARNER_TYPE -> {
                val itemPosition = dialog.arguments?.getInt(ARGUMENT_KEY_ITEM_POSITION) ?: return
                val measure = adapter.getItemByPosition(itemPosition)
                updateWarnerType(measure, SensorConfiguration.Measure.ensureAvailableWarnerType(position))
            }
            DIALOG_TAG_CHOOSE_CORRECTOR_TYPE -> {
                val itemPosition = dialog.arguments?.getInt(ARGUMENT_KEY_ITEM_POSITION) ?: return
                val measure = adapter.getItemByPosition(itemPosition)
                updateCorrectorType(measure, SensorConfiguration.Measure.ensureAvailableCorrectorType(position))
            }
            DIALOG_TAG_CHOOSE_LINEAR_FITTING_CORRECTOR_MODIFY_TYPE -> {
                val arguments = dialog.arguments ?: return
                val itemPosition = arguments.getInt(ARGUMENT_KEY_ITEM_POSITION)
                val groupPosition = arguments.getInt(ARGUMENT_KEY_LINEAR_FITTING_CORRECTOR_ITEM_POSITION)
                val measure = adapter.getItemByPosition(itemPosition)
                val corrector = measure.configuration.corrector as? LinearFittingCorrector
                when (position) {
                    0 -> showUpdateCorrectedValueDialog(corrector, groupPosition, itemPosition, null, true)
                    1 -> showUpdateCorrectedValueDialog(corrector, groupPosition + 1, itemPosition, null, true)
                    2 -> deleteLinearFittingCorrectorItemGroup(measure, groupPosition)
                }
            }
        }
    }

//    override fun onConfirm(dialog: BaseServiceDialog<*>): Boolean {
//        if (super.onConfirm(dialog)) {
//            return true
//        }
//        when (dialog.tag) {
//            DIALOG_TAG_INPUT_VALUE_EMPTY, DIALOG_TAG_INPUT_VALUE_ERROR -> return true
//        }
//        return true
//    }

    private fun insertWarnerType(measure: SensorConfiguration.Measure, warnerType: Int) {
        if (measure.configurationId == 0L) {
            val args = Bundle()
            args.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
            args.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measure.id)
            args.putInt(ARGUEMENT_KEY_WARNER_TYPE, warnerType)
            //replaceMeasurementConfig(measure, ContentValues(), TOKEN_ADD_MEASUREMENT_CONFIG, args)
            addMeasurementConfig(measure, args)
        } else {
            insertWarnerType(measure.configurationId, warnerType)
        }
    }

//    private fun insertWarnerType(measurementConfigId: Long, measurementId: Long, measureType: Int) {
//        val warnerType = SensorConfiguration.Measure.getWarnerType(measureType)
//        if (measurementConfigId == 0L) {
//            val args = Bundle()
//            args.putInt(ARGUMENT_KEY_TOKEN, TOKEN_INSERT_WARNER_TYPE)
//            args.putLong(ARGUMENT_KEY_MEASUREMENT_ID, measurementId)
//            args.putInt(ARGUEMENT_KEY_WARNER_TYPE, warnerType)
//            //replaceMeasurementConfig(measure, )
//            addMeasurementConfig(measurementId, measureType, args, null)
//        } else {
//            insertWarnerType(measurementConfigId, warnerType)
//        }
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
            else -> getString(R.string.type_none)
        }
    }

//    private fun getMeasurementId(dialog: BaseServiceDialog<*>) =
//            dialog.arguments?.getLong(ARGUMENT_KEY_MEASUREMENT_ID, 0L) ?: 0L
//
//    private fun getMeasurementConfigId(dialog: BaseServiceDialog<*>) =
//            dialog.arguments?.getLong(ARGUMENT_KEY_MEASUREMENT_CONFIG_ID, 0L) ?: 0L
//
//    private fun getMeasurementType(dialog: BaseServiceDialog<*>) =
//            dialog.arguments?.getInt(ARGUMENT_KEY_MEASUREMENT_TYPE, 0) ?: 0

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
    }

    override fun onExecSqlComplete(token: Int, cookie: Any?, result: Boolean) {
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
            val errStringRes = getPromptErrorInfo(token, cookie)
            if (errStringRes != 0) {
                SimpleCustomizeToast.show(errStringRes)
            }
        }
    }

    private fun getPromptErrorInfo(token: Int, cookie: Any?): Int {
        return when (token) {
            TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME1 -> R.string.measurement_custom_name_modify_failed
            TOKEN_UPDATE_MEASUREMENT_CUSTOM_UNIT -> R.string.measurement_custom_unit_modify_failed
            TOKEN_UPDATE_MEASUREMENT_DECIMALS -> R.string.measurement_decimals_modify_failed
            TOKEN_ADD_MEASUREMENT_CONFIG -> {
                if (cookie is Bundle) {
                    val innerToken = cookie.getInt(ARGUMENT_KEY_TOKEN)
                    if (innerToken > 0) {
                        getPromptErrorInfo(innerToken, null)
                    } else {
                        R.string.add_measurement_config_failed
                    }
                } else {
                    R.string.add_measurement_config_failed
                }
            }
            TOKEN_INSERT_WARNER_TYPE -> R.string.warner_type_modify_failed

            //TOKEN_ADD_MEASUREMENT_CONFIG -> R.string.measurement_custom_name_modify_failed
            TOKEN_UPDATE_HIGH_LIMIT -> R.string.set_high_limit_failed
            TOKEN_UPDATE_LOW_LIMIT -> R.string.set_low_limit_failed
            TOKEN_UPDATE_ABNORMAL_VALUE -> R.string.set_abnormal_value_failed
            TOKEN_UPDATE_INITIAL_VALUE -> R.string.set_initial_value_failed
            TOKEN_UPDATE_INITIAL_DISTANCE -> R.string.set_initial_distance_failed
            TOKEN_UPDATE_CORRECTOR_TYPE -> R.string.modify_measurement_corrector_type_failed
            TOKEN_INSERT_LINEAR_FITTING_CORRECTOR -> R.string.insert_linear_fitting_failed
            TOKEN_INSERT_LINEAR_FITTING_CORRECTOR_ITEM_GROUP -> R.string.insert_linear_fitting_corrector_item_group_failed
            TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_CORRECTED_VALUE -> R.string.update_linear_fitting_corrector_corrected_value_failed
            TOKEN_UPDATE_LINEAR_FITTING_CORRECTOR_SAMPLING_VALUE -> R.string.update_linear_fitting_corrector_sampling_value_failed
            TOKEN_DELETE_LINEAR_FITTING_CORRECTOR_ITEM_GROUP -> R.string.delete_linear_fitting_corrector_item_group_failed
            else -> 0
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
            val errStringRes = getPromptErrorInfo(token, null)
//            val errStringRes = when (token) {
//                TOKEN_UPDATE_MEASUREMENT_CUSTOM_NAME1 -> R.string.measurement_custom_name_modify_failed
//                TOKEN_UPDATE_MEASUREMENT_CUSTOM_UNIT -> R.string.measurement_custom_unit_modify_failed
//                TOKEN_UPDATE_MEASUREMENT_DECIMALS -> R.string.measurement_decimals_modify_failed
//                TOKEN_UPDATE_SENSOR_CUSTOM_NAME ->  R.string.sensor_custom_name_modify_failed
//                TOKEN_UPDATE_INITIAL_VALUE -> R.string.set_initial_value_failed
//                TOKEN_UPDATE_INITIAL_DISTANCE -> R.string.set_initial_distance_failed
//                else -> 0
//            }
            if (errStringRes != 0) {
                SimpleCustomizeToast.show(errStringRes)
            }
        }
    }

    private fun refreshSensorConfig() {
        LoaderManager.getInstance(this).getLoader<SensorConfiguration>(0)?.onContentChanged()
    }
}
