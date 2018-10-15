package com.weisi.tool.wsnbox.io;

/**
 * Created by CJQ on 2018/3/5.
 */

public interface Constant {
    String TAG_SENSOR = "sensor";
    String TAG_MEASUREMENT = "measurement";
    String TAG_ADDRESS = "address";
    String TAG_NAME = "name";
    String TAG_TYPE = "type";
    String TAG_WARNER = "warner";
    String TAG_DEVICE = "device";
    String TAG_NODE = "node";
    String TAG_INDEX = "index";
    String TAG_NAMES = "names";
    String TAG_ADDRESSES = "addresses";
    String TABLE_SENSOR_INIT_DATA = "sensor_init_data";
    String TABLE_MEASUREMENT_INIT_DATA = "measurement_init_data";
    String TABLE_SENSOR_DATA = "sensor_data";
    String TABLE_MEASUREMENT_DATA = "measurement_data";
    String TABLE_CONFIGURATION_PROVIDER = "config_provider";
    String TABLE_SENSOR_CONFIGURATION = "sensor_config";
    String TABLE_MEASUREMENT_CONFIGURATION = "measurement_config";
    String TABLE_GENERAL_SINGLE_RANGE_WARNER = "gsr_warner";
    String TABLE_GENERAL_SWITCH_WARNER = "gs_warner";
    String TABLE_DEVICE = TAG_DEVICE;
    String TABLE_NODE = TAG_NODE;
    String TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION = "ratchet_cfg";
    String COLUMN_SENSOR_ADDRESS = TAG_ADDRESS;
    String COLUMN_TIMESTAMP = "time";
    String COLUMN_BATTER_VOLTAGE = "voltage";
    String COLUMN_RAW_VALUE = "value";
    String COLUMN_MEASUREMENT_VALUE_ID = "value_id";
    String COLUMN_CONFIGURATION_PROVIDER_ID = "provider_id";
    String COLUMN_CONFIGURATION_PROVIDER_NAME = TAG_NAME;
    String COLUMN_CREATE_TIME = "create_time";
    String COLUMN_MODIFY_TIME = "modify_time";
    String COLUMN_CUSTOM_NAME = "custom_name";
    String COLUMN_COMMON_ID = "id";
    String COLUMN_FUNCTION_ID = "fun_id";
    //private static final String COLUMN_WARN_TYPE = "warn_type";
    String COLUMN_SENSOR_CONFIGURATION_ID = "sensor_cfg_id";
    String COLUMN_MEASUREMENT_CONFIGURATION_ID = "measure_cfg_id";
    String COLUMN_LOW_LIMIT = "low_limit";
    String COLUMN_HIGH_LIMIT = "high_limit";
    String COLUMN_ABNORMAL_VALUE = "abnormal_value";
    String COLUMN_DEVICE_NAME = TAG_NAME;
    String COLUMN_NODE_NAME = TAG_NAME;
    String COLUMN_DEVICE_ID = "device_id";
    String COLUMN_TYPE = TAG_TYPE;
    String COLUMN_INITIAL_DISTANCE = "init_distance";
    String COLUMN_INITIAL_VALUE = "init_value";
}
