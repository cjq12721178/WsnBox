package com.weisi.tool.wsnbox.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.Configuration;
import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.ID;
import com.cjq.lib.weisi.iot.Measurement;
import com.cjq.lib.weisi.iot.PracticalMeasurement;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.lib.weisi.iot.SensorManager;
import com.cjq.lib.weisi.iot.Warner;
import com.cjq.tool.qbox.database.SQLiteResolverDelegate;
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.CommonValueContainerConfigurationProvider;
import com.weisi.tool.wsnbox.bean.configuration.DisplayMeasurementConfiguration;
import com.weisi.tool.wsnbox.bean.configuration.RatchetWheelMeasurementConfiguration;
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration;
import com.weisi.tool.wsnbox.bean.configuration.SensorInfoConfiguration;
import com.weisi.tool.wsnbox.bean.data.Device;
import com.weisi.tool.wsnbox.bean.data.Node;
import com.weisi.tool.wsnbox.bean.data.SensorData;
import com.weisi.tool.wsnbox.bean.decorator.CommonMeasurementDecorator;
import com.weisi.tool.wsnbox.bean.decorator.CommonSensorInfoDecorator;
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner;
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner;
import com.weisi.tool.wsnbox.io.Constant;
import com.weisi.tool.wsnbox.util.FlavorClassBuilder;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by CJQ on 2017/11/9.
 */

public class SensorDatabase implements Constant {

    private static final String DATABASE_NAME = "SensorStorage.db";
    //数据库初始版本，存储传感器历史数据
    private static final int VN_WINDOW = 1;
    //增加对传感器配置的管理
    private static final int VN_CONFIGURATION = 2;
    //增加可配置的设备节点功能
    private static final int VN_DEVICE_NODE = 3;
    //为修复酒钢数据时间戳超前一个月问题特订
    private static final int VN_BROKEN_TIME = 4;
    //增加虚拟测量量配置
    private static final int VN_VIRTUAL_CONFIG = 5;
    //修改测量量配置字段（type）类型
    private static final int VN_GOOD_TYPE = 6;
    //移除棘轮测量量配置表字段（custom_name)
    private static final int VN_THIN_RATCHET_WHEEL = 7;
    //修复棘轮测量量配置表未创建的问题
    private static final int VN_NO_RATCHET_WHEEL = 8;
    private static final int CURRENT_VERSION_NO = VN_NO_RATCHET_WHEEL;

    private static SQLiteLauncher launcher;
    private static SQLiteDatabase database;

    private SensorDatabase() {
    }

    public static boolean launch(Context context) {
        if (database != null)
            return true;
        if (context == null)
            return false;
        try {
            if (launcher == null) {
                launcher = new SQLiteLauncher(context, DATABASE_NAME, CURRENT_VERSION_NO);
            }
            database = launcher.getWritableDatabase();
            setEnableForeignKeyConstraints(true);
        } catch (Exception e) {
            ExceptionLog.process(e);
        }
        return database != null;
    }

    private static void setEnableForeignKeyConstraints(boolean enabled) {
        String enableForeignKeyConstraints = "PRAGMA foreign_keys = " + (enabled ? "ON" : "OFF");
        database.execSQL(enableForeignKeyConstraints);
    }

    public static void shutdown() {
        if (launcher != null) {
            launcher.close();
            database = null;
            launcher = null;
        }
    }

    public static boolean importSensorHistoryValues(long id,
                                                    long startTime,
                                                    long endTime,
                                                    int limitCount,
                                                    SensorHistoryInfoReceiver receiver) {
        if (ID.isPhysicalSensor(id)) {
            //Log.d(Tag.LOG_TAG_D_TEST, "before importSensorHistoryValues");
            return importPhysicalSensorHistoryValues(ID.getAddress(id), startTime, endTime, limitCount, receiver);
        }
        if (ID.isLogicalSensor(id)) {
            return importLogicalSensorHistoryValues(id, startTime, endTime, limitCount, receiver);
        }
        return false;
    }

    private static boolean importPhysicalSensorHistoryValues(int address,
                                                             long startTime,
                                                             long endTime,
                                                             int limitCount,    //分批获取数量
                                                             SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null || startTime > endTime) {
            return false;
        }
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            int limit = limitCount <= 0 ? 10 : limitCount;
            int offset = 0;
            boolean needSearch;
            do {
                //导入传感器历史数据
                builder.setLength(0);
                builder.append("SELECT ").append(COLUMN_TIMESTAMP)
                        .append(',').append(COLUMN_BATTER_VOLTAGE)
                        .append(" FROM ").append(TABLE_SENSOR_DATA)
                        .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
                        .append(" = ").append(address);
                if (startTime > 0) {
                    builder.append(" AND ");
                    if (endTime > 0) {
                        builder.append('(').append(COLUMN_TIMESTAMP)
                                .append(" BETWEEN ").append(startTime)
                                .append(" AND ").append(endTime)
                                .append(')');
                    } else {
                        builder.append(COLUMN_TIMESTAMP)
                                .append(" >= ")
                                .append(startTime);
                    }
                } else if (endTime > 0) {
                    builder.append(" AND ")
                            .append(COLUMN_TIMESTAMP)
                            .append(" < ")
                            .append(endTime);
                }
                builder.append(" ORDER BY ").append(COLUMN_TIMESTAMP)
                        .append(" LIMIT ").append(limit)
                        .append(" OFFSET ").append(offset);
                cursor = database.rawQuery(builder.toString(), null);
                if (cursor == null) {
                    return offset > 0;
                }
                int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                int voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
                while (cursor.moveToNext()) {
                    if (cursor.isFirst()) {
                        startTime = cursor.getLong(timestampIndex);
                    } else if (cursor.isLast()) {
                        endTime = cursor.getLong(timestampIndex);
                    }
                    receiver.onSensorDataReceived(
                            address,
                            cursor.getLong(timestampIndex),
                            cursor.getFloat(voltageIndex)
                    );
                }
                needSearch = cursor.getCount() == limit;
                offset += limit;
                cursor.close();

                //导入传感器测量量历史数据
                builder.setLength(0);
                builder.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                        .append(',').append(COLUMN_TIMESTAMP)
                        .append(',').append(COLUMN_RAW_VALUE)
                        .append(" FROM ").append(TABLE_MEASUREMENT_DATA)
                        .append(" WHERE (").append(COLUMN_MEASUREMENT_VALUE_ID)
                        .append(" BETWEEN ").append(((long) address) << 32)
                        .append(" AND ").append(((long) (address + 1)) << 32)
                        .append(") AND ")
                        .append('(').append(COLUMN_TIMESTAMP)
                        .append(" BETWEEN ").append(startTime)
                        .append(" AND ").append(endTime).append(')')
                        .append(" ORDER BY ").append(COLUMN_TIMESTAMP);
                cursor = database.rawQuery(builder.toString(), null);
                if (cursor == null) {
                    return offset > 0;
                }
                int measurementValueIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                int rawValueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
                while (cursor.moveToNext()) {
                    receiver.onMeasurementDataReceived(
                            cursor.getLong(measurementValueIdIndex),
                            cursor.getLong(timestampIndex),
                            cursor.getDouble(rawValueIndex)
                    );
                }
                cursor.close();
            } while (needSearch);
            //Log.d(Tag.LOG_TAG_D_TEST, "after importSensorHistoryValues");
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

//    private static boolean importPhysicalSensorHistoryValues(int address,
//                                                            long startTime,
//                                                            long endTime,
//                                                            int limitCount,
//                                                            SensorHistoryInfoReceiver receiver) {
//        if (database == null || receiver == null || startTime > endTime) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            StringBuffer buffer = new StringBuffer();
//            //导入传感器历史数据
//            buffer.append("SELECT ").append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_BATTER_VOLTAGE)
//                    .append(" FROM ").append(TABLE_SENSOR_DATA)
//                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(" = ").append(address);
//            if (startTime > 0) {
//                buffer.append(" AND ");
//                if (endTime > 0) {
//                    buffer.append('(').append(COLUMN_TIMESTAMP)
//                            .append(" BETWEEN ").append(startTime)
//                            .append(" AND ").append(endTime)
//                            .append(')');
//                } else {
//                    buffer.append(COLUMN_TIMESTAMP)
//                            .append(" >= ")
//                            .append(startTime);
//                }
//            } else if (endTime > 0) {
//                buffer.append(" AND ")
//                        .append(COLUMN_TIMESTAMP)
//                        .append(" < ")
//                        .append(endTime);
//            }
//            buffer.append(" ORDER BY ").append(COLUMN_TIMESTAMP);
//            if (limitCount > 0) {
//                buffer.append(" LIMIT ").append(limitCount);
//            }
//            cursor = database.rawQuery(buffer.toString(), null);
//            if (cursor == null) {
//                return false;
//            }
//            int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//            int voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
//            while (cursor.moveToNext()) {
//                if (cursor.isFirst()) {
//                    startTime = cursor.getLong(timestampIndex);
//                } else if (cursor.isLast()) {
//                    endTime = cursor.getLong(timestampIndex);
//                }
//                receiver.onSensorDataReceived(
//                        address,
//                        cursor.getLong(timestampIndex),
//                        cursor.getFloat(voltageIndex)
//                );
//            }
//            cursor.close();
//            //导入传感器测量量历史数据
//            buffer.setLength(0);
//            buffer.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_RAW_VALUE)
//                    .append(" FROM ").append(TABLE_MEASUREMENT_DATA)
//                    .append(" WHERE (").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(" BETWEEN ").append(((long) address) << 32)
//                    .append(" AND ").append(((long) (address + 1)) << 32)
//                    .append(") AND ")
//                    .append('(').append(COLUMN_TIMESTAMP)
//                    .append(" BETWEEN ").append(startTime)
//                    .append(" AND ").append(endTime).append(')')
//                    .append(" ORDER BY ").append(COLUMN_TIMESTAMP);
//            cursor = database.rawQuery(buffer.toString(), null);
//            if (cursor == null) {
//                return false;
//            }
//            int measurementValueIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
//            timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//            int rawValueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
//            while (cursor.moveToNext()) {
//                receiver.onMeasurementDataReceived(
//                        cursor.getLong(measurementValueIdIndex),
//                        cursor.getLong(timestampIndex),
//                        cursor.getDouble(rawValueIndex)
//                );
//            }
//            cursor.close();
//            return true;
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

    private static boolean importLogicalSensorHistoryValues(long id,
                                                            long startTime,
                                                            long endTime,
                                                            int limitCount,
                                                            SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null || startTime > endTime) {
            return false;
        }
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            int limit = limitCount <= 0 ? 10 : limitCount;
            int offset = 0;
            boolean needSearch;
            do {
                //导入逻辑传感器历史数据
                builder.append("SELECT ").append(COLUMN_TIMESTAMP)
                        .append(',').append(COLUMN_RAW_VALUE)
                        .append(" FROM ").append(TABLE_MEASUREMENT_DATA)
                        .append(" WHERE ").append(COLUMN_MEASUREMENT_VALUE_ID)
                        .append(" = ").append(id);
                if (startTime > 0) {
                    builder.append(" AND ");
                    if (endTime > 0) {
                        builder.append('(').append(COLUMN_TIMESTAMP)
                                .append(" BETWEEN ").append(startTime)
                                .append(" AND ").append(endTime)
                                .append(')');
                    } else {
                        builder.append(COLUMN_TIMESTAMP)
                                .append(" >= ")
                                .append(startTime);
                    }
                } else if (endTime > 0) {
                    builder.append(" AND ")
                            .append(COLUMN_TIMESTAMP)
                            .append(" < ")
                            .append(endTime);
                }
                builder.append(" ORDER BY ").append(COLUMN_TIMESTAMP)
                        .append(" LIMIT ").append(limit)
                        .append(" OFFSET ").append(offset);
                cursor = database.rawQuery(builder.toString(), null);
                if (cursor == null) {
                    return offset > 0;
                }
                int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                int rawValueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
                while (cursor.moveToNext()) {
                    if (cursor.isFirst()) {
                        startTime = cursor.getLong(timestampIndex);
                    } else if (cursor.isLast()) {
                        endTime = cursor.getLong(timestampIndex);
                    }
                    receiver.onMeasurementDataReceived(
                            id,
                            cursor.getLong(timestampIndex),
                            cursor.getDouble(rawValueIndex)
                    );
                }
                needSearch = cursor.getCount() == limit;
                offset += limit;
                cursor.close();
                //导入物理传感器历史数据
                int address = ID.getAddress(id);
                builder.setLength(0);
                builder.append("SELECT ").append(COLUMN_TIMESTAMP)
                        .append(',').append(COLUMN_BATTER_VOLTAGE)
                        .append(" FROM ").append(TABLE_SENSOR_DATA)
                        .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
                        .append(" = ").append(address)
                        .append(" AND ")
                        .append('(').append(COLUMN_TIMESTAMP)
                        .append(" BETWEEN ").append(startTime)
                        .append(" AND ").append(endTime).append(')')
                        .append(" ORDER BY ").append(COLUMN_TIMESTAMP);
                cursor = database.rawQuery(builder.toString(), null);
                if (cursor == null) {
                    return offset > 0;
                }
                timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                int batteryVoltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
                while (cursor.moveToNext()) {
                    receiver.onSensorDataReceived(
                            address,
                            cursor.getLong(timestampIndex),
                            cursor.getFloat(batteryVoltageIndex)
                    );
                }
                cursor.close();
            } while (needSearch);
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

//    private static boolean importLogicalSensorHistoryValues(long id,
//                                                            long startTime,
//                                                            long endTime,
//                                                            int limitCount,
//                                                            SensorHistoryInfoReceiver receiver) {
//        if (database == null || receiver == null || startTime > endTime) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            StringBuffer buffer = new StringBuffer();
//            //导入逻辑传感器历史数据
//            buffer.append("SELECT ").append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_RAW_VALUE)
//                    .append(" FROM ").append(TABLE_MEASUREMENT_DATA)
//                    .append(" WHERE ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(" = ").append(id);
//            if (startTime > 0) {
//                buffer.append(" AND ");
//                if (endTime > 0) {
//                    buffer.append('(').append(COLUMN_TIMESTAMP)
//                            .append(" BETWEEN ").append(startTime)
//                            .append(" AND ").append(endTime)
//                            .append(')');
//                } else {
//                    buffer.append(COLUMN_TIMESTAMP)
//                            .append(" >= ")
//                            .append(startTime);
//                }
//            } else if (endTime > 0) {
//                buffer.append(" AND ")
//                        .append(COLUMN_TIMESTAMP)
//                        .append(" < ")
//                        .append(endTime);
//            }
//            buffer.append(" ORDER BY ").append(COLUMN_TIMESTAMP);
//            if (limitCount > 0) {
//                buffer.append(" LIMIT ").append(limitCount);
//            }
//            cursor = database.rawQuery(buffer.toString(), null);
//            if (cursor == null) {
//                return false;
//            }
//            int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//            int rawValueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
//            while (cursor.moveToNext()) {
//                if (cursor.isFirst()) {
//                    startTime = cursor.getLong(timestampIndex);
//                } else if (cursor.isLast()) {
//                    endTime = cursor.getLong(timestampIndex);
//                }
//                receiver.onMeasurementDataReceived(
//                        id,
//                        cursor.getLong(timestampIndex),
//                        cursor.getDouble(rawValueIndex)
//                );
//            }
//            cursor.close();
//            //导入物理传感器历史数据
//            int address = ID.getAddress(id);
//            buffer.setLength(0);
//            buffer.append("SELECT ").append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_BATTER_VOLTAGE)
//                    .append(" FROM ").append(TABLE_SENSOR_DATA)
//                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(" = ").append(address)
//                    .append(" AND ")
//                    .append('(').append(COLUMN_TIMESTAMP)
//                    .append(" BETWEEN ").append(startTime)
//                    .append(" AND ").append(endTime).append(')')
//                    .append(" ORDER BY ").append(COLUMN_TIMESTAMP);
//            cursor = database.rawQuery(buffer.toString(), null);
//            if (cursor == null) {
//                return false;
//            }
//            timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//            int batteryVoltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
//            while (cursor.moveToNext()) {
//                receiver.onSensorDataReceived(
//                        address,
//                        cursor.getLong(timestampIndex),
//                        cursor.getFloat(batteryVoltageIndex)
//                );
//            }
//            cursor.close();
//            return true;
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

    //返回-1代表操作失败
    public static int getPhysicalSensorWithHistoryValueCount() {
        if (database == null) {
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT COUNT(0) AS size FROM " + TABLE_SENSOR_INIT_DATA, null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex("size"));
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    public static boolean importSensorEarliestValue(SensorHistoryInfoReceiver receiver, boolean physicalFirst) {
        return physicalFirst
                ? onlyImportPhysicalSensorEarliestValue(receiver) && onlyImportLogicalSensorEarliestValue(receiver)
                : onlyImportLogicalSensorEarliestValue(receiver) && onlyImportPhysicalSensorEarliestValue(receiver);
//        if (database == null || receiver == null) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            final int SELECT_LIMIT_COUNT = 10;
//            StringBuffer buffer = new StringBuffer();
//            //导入具有历史数据的传感器及其首条历史数据
//            buffer.append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_BATTER_VOLTAGE)
//                    .append(" FROM ").append(TABLE_SENSOR_INIT_DATA);
//            int prefixLength = buffer.length();
//            int startPosition = 0, addressIndex = 0,
//                    timestampIndex = 0, voltageIndex = 0;
//            do {
//                if (cursor != null) {
//                    cursor.close();
//                }
//                buffer.setLength(prefixLength);
//                cursor = database.rawQuery(buffer.append(" LIMIT ")
//                        .append(startPosition).append(',')
//                        .append(SELECT_LIMIT_COUNT)
//                        .toString(), null);
//                if (cursor == null) {
//                    return false;
//                }
//                if (startPosition == 0) {
//                    addressIndex = cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS);
//                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//                    voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
//                }
//                while (cursor.moveToNext()) {
//                    receiver.onSensorDataReceived(
//                            cursor.getInt(addressIndex),
//                            cursor.getLong(timestampIndex),
//                            cursor.getFloat(voltageIndex)
//                    );
//                }
//                startPosition += SELECT_LIMIT_COUNT;
//            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
//            //导入相应传感器测量量的首条历史数据
//            buffer.setLength(0);
//            buffer.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_RAW_VALUE)
//                    .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA);
//            prefixLength = buffer.length();
//            startPosition = 0;
//            int measurementIdIndex = 0, valueIndex = 0;
//            do {
//                if (cursor != null) {
//                    cursor.close();
//                }
//                buffer.setLength(prefixLength);
//                cursor = database.rawQuery(buffer.append(" LIMIT ")
//                        .append(startPosition).append(',')
//                        .append(SELECT_LIMIT_COUNT)
//                        .toString(), null);
//                if (cursor == null) {
//                    return false;
//                }
//                if (startPosition == 0) {
//                    measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
//                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//                    valueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
//                }
//                while (cursor.moveToNext()) {
//                    receiver.onMeasurementDataReceived(
//                            cursor.getLong(measurementIdIndex),
//                            cursor.getLong(timestampIndex),
//                            cursor.getDouble(valueIndex)
//                    );
//                }
//                startPosition += SELECT_LIMIT_COUNT;
//            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
//            return true;
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return false;
    }

    public static boolean onlyImportPhysicalSensorEarliestValue(SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            final int SELECT_LIMIT_COUNT = 10;
            StringBuilder builder = new StringBuilder();
            //导入具有历史数据的传感器及其首条历史数据
            builder.append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(" FROM ").append(TABLE_SENSOR_INIT_DATA);
            int prefixLength = builder.length();
            int startPosition = 0, addressIndex = 0,
                    timestampIndex = 0, voltageIndex = 0;
            do {
                if (cursor != null) {
                    cursor.close();
                }
                builder.setLength(prefixLength);
                cursor = database.rawQuery(builder.append(" LIMIT ")
                        .append(startPosition).append(',')
                        .append(SELECT_LIMIT_COUNT)
                        .toString(), null);
                if (cursor == null) {
                    return false;
                }
                if (startPosition == 0) {
                    addressIndex = cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS);
                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                    voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
                }
                while (cursor.moveToNext()) {
                    receiver.onSensorDataReceived(
                            cursor.getInt(addressIndex),
                            cursor.getLong(timestampIndex),
                            cursor.getFloat(voltageIndex)
                    );
                }
                startPosition += SELECT_LIMIT_COUNT;
            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    public static boolean onlyImportLogicalSensorEarliestValue(SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            final int SELECT_LIMIT_COUNT = 10;
            StringBuilder builder = new StringBuilder();
            //导入相应传感器测量量的首条历史数据
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_RAW_VALUE)
                    .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA);
            int prefixLength = builder.length();
            int startPosition = 0;
            int measurementIdIndex = 0, timestampIndex = 0, valueIndex = 0;
            do {
                if (cursor != null) {
                    cursor.close();
                }
                builder.setLength(prefixLength);
                cursor = database.rawQuery(builder.append(" LIMIT ")
                        .append(startPosition).append(',')
                        .append(SELECT_LIMIT_COUNT)
                        .toString(), null);
                if (cursor == null) {
                    return false;
                }
                if (startPosition == 0) {
                    measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
                    valueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
                }
                while (cursor.moveToNext()) {
                    receiver.onMeasurementDataReceived(
                            cursor.getLong(measurementIdIndex),
                            cursor.getLong(timestampIndex),
                            cursor.getDouble(valueIndex)
                    );
                }
                startPosition += SELECT_LIMIT_COUNT;
            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    //返回-1代表操作失败
    public static int getLogicalSensorWithHistoryValueCount() {
        if (database == null) {
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT COUNT(0) AS size FROM " + TABLE_MEASUREMENT_INIT_DATA, null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex("size"));
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

//    public static boolean importLogicalSensorEarliestValue(SensorHistoryInfoReceiver receiver) {
//        if (database == null || receiver == null) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            final int SELECT_LIMIT_COUNT = 10;
//            StringBuffer buffer = new StringBuffer();
//            //导入具有历史数据的测量量及其首条历史数据
//            buffer.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_RAW_VALUE)
//                    .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA);
//            int prefixLength = buffer.length();
//            int startPosition = 0, idIndex = 0,
//                    timestampIndex = 0, valueIndex = 0;
//            do {
//                if (cursor != null) {
//                    cursor.close();
//                }
//                buffer.setLength(prefixLength);
//                cursor = database.rawQuery(buffer.append(" LIMIT ")
//                        .append(startPosition).append(',')
//                        .append(SELECT_LIMIT_COUNT)
//                        .toString(), null);
//                if (cursor == null) {
//                    return false;
//                }
//                if (startPosition == 0) {
//                    idIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
//                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//                    valueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
//                }
//                while (cursor.moveToNext()) {
//                    receiver.onMeasurementDataReceived(
//                            cursor.getLong(idIndex),
//                            cursor.getLong(timestampIndex),
//                            cursor.getDouble(valueIndex)
//                    );
//                }
//                startPosition += SELECT_LIMIT_COUNT;
//            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
//            //导入所属物理传感器的首条历史数据
//            buffer.setLength(0);
//            buffer.append("SELECT ").append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_BATTER_VOLTAGE)
//                    .append(" FROM ").append(TABLE_SENSOR_INIT_DATA);
//            prefixLength = buffer.length();
//            startPosition = 0;
//            int batteryIndex = 0;
//            do {
//                if (cursor != null) {
//                    cursor.close();
//                }
//                buffer.setLength(prefixLength);
//                cursor = database.rawQuery(buffer.append(" LIMIT ")
//                        .append(startPosition).append(',')
//                        .append(SELECT_LIMIT_COUNT)
//                        .toString(), null);
//                if (cursor == null) {
//                    return false;
//                }
//                if (startPosition == 0) {
//                    timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//                    batteryIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
//                }
//                while (cursor.moveToNext()) {
//                    receiver.onSensorDataReceived(
//                            cursor.getLong(measurementIdIndex),
//                            cursor.getLong(timestampIndex),
//                            cursor.getDouble(valueIndex)
//                    );
//                }
//                startPosition += SELECT_LIMIT_COUNT;
//            } while (cursor.getCount() == SELECT_LIMIT_COUNT);
//            return true;
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

    public interface SensorHistoryInfoReceiver {
        void onSensorDataReceived(int address, long timestamp, float batteryVoltage);
        void onMeasurementDataReceived(long measurementValueId, long timestamp, double rawValue);
    }

    public static boolean batchSaveSensorData(SensorDataProvider provider) {
        if (database == null || provider == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            StringBuilder sensorDataBuilder = new StringBuilder();
            StringBuilder measurementDataBuilder = new StringBuilder();
            database.beginTransaction();
            sensorDataBuilder.append("INSERT OR IGNORE INTO ");
            measurementDataBuilder.append(sensorDataBuilder);
            int insertPrefixLen = sensorDataBuilder.length();
            sensorDataBuilder.append(TABLE_SENSOR_DATA)
                    .append(" (").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(") VALUES (");
            String sensorDataNormalInsertSentence = sensorDataBuilder.toString();
            int sensorDataNormalInsertWholePrefixLength = sensorDataBuilder.length();
            measurementDataBuilder.append(TABLE_MEASUREMENT_DATA)
                    .append(" (").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_RAW_VALUE)
                    .append(") VALUES (");
            String measurementDataNormalInsertSentence = measurementDataBuilder.toString();
            int measurementDataNormalInsertWholePrefixLength = measurementDataBuilder.length();
            boolean sensorDataInsertSentenceChanged = false;
            boolean measurementDataInsertSentenceChanged = false;
            SensorData sensorData;
            for (int i = 0,
                 count = provider.getSensorDataCount();
                 (count <= 0 || i < count)
                         && (sensorData = provider.provideSensorData()) != null;
                 ++i) {
                switch (provider.getSensorDataState(sensorData)) {
                    case SensorDataProvider.DUPLICATE_DATA:
                        continue;
                    case SensorDataProvider.NORMAL_DATA:
                        if (sensorDataInsertSentenceChanged) {
                            sensorDataBuilder.replace(insertPrefixLen,
                                    insertPrefixLen + TABLE_SENSOR_INIT_DATA.length(),
                                    TABLE_SENSOR_DATA);
                            sensorDataInsertSentenceChanged = false;
                        }
                        if (measurementDataInsertSentenceChanged) {
                            measurementDataBuilder.replace(insertPrefixLen,
                                    insertPrefixLen + TABLE_MEASUREMENT_INIT_DATA.length(),
                                    TABLE_MEASUREMENT_DATA);
                            measurementDataInsertSentenceChanged = false;
                        }
                        sensorDataBuilder.setLength(sensorDataNormalInsertWholePrefixLength);
                        measurementDataBuilder.setLength(measurementDataNormalInsertWholePrefixLength);
                        break;
                    case SensorDataProvider.FIRST_DATA:
                        sensorDataBuilder.setLength(0);
                        cursor = database.rawQuery(sensorDataBuilder
                                .append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
                                .append(" FROM ").append(TABLE_SENSOR_INIT_DATA)
                                .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
                                .append(" = ").append(sensorData.getAddress())
                                .toString(), null);
                        sensorDataBuilder.setLength(0);
                        sensorDataBuilder.append(sensorDataNormalInsertSentence);
                        if (cursor == null
                                || !cursor.moveToNext()
                                || cursor.getInt(cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS)) != sensorData.getAddress()) {
                            sensorDataBuilder.replace(insertPrefixLen, insertPrefixLen + TABLE_SENSOR_DATA.length(), TABLE_SENSOR_INIT_DATA);
                            sensorDataInsertSentenceChanged = true;
                        } else {
                            sensorDataInsertSentenceChanged = false;
                        }
                        measurementDataBuilder.setLength(0);
                        if (cursor != null) {
                            cursor.close();
                        }
                        cursor = database.rawQuery(measurementDataBuilder
                                .append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                                .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA)
                                .append(" WHERE ").append(COLUMN_MEASUREMENT_VALUE_ID)
                                .append(" = ").append(sensorData.getId())
                                .toString(), null);
                        measurementDataBuilder.setLength(0);
                        measurementDataBuilder.append(measurementDataNormalInsertSentence);
                        if (cursor == null
                                || !cursor.moveToNext()
                                || cursor.getLong(cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID)) != sensorData.getId()) {
                            measurementDataBuilder.replace(insertPrefixLen, insertPrefixLen + TABLE_MEASUREMENT_DATA.length(), TABLE_MEASUREMENT_INIT_DATA);
                            measurementDataInsertSentenceChanged = true;
                        } else {
                            measurementDataInsertSentenceChanged = false;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        break;
                }
                sensorDataBuilder.append(sensorData.getAddress())
                        .append(',').append(sensorData.getTimestamp())
                        .append(',').append(sensorData.getBatteryVoltage())
                        .append(')');
                database.execSQL(sensorDataBuilder.toString());
                measurementDataBuilder.append(sensorData.getId())
                        .append(',').append(sensorData.getTimestamp())
                        .append(',').append(sensorData.getRawValue())
                        .append(')');
                database.execSQL(measurementDataBuilder.toString());
                sensorData.recycle();
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            database.endTransaction();
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    public static long getConfigurationProviderNextId() {
        return getNextAutoIncrementId(TABLE_CONFIGURATION_PROVIDER);
    }

    private static long getNextAutoIncrementId(String tableName) {
        if (database == null || TextUtils.isEmpty(tableName)) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT seq FROM sqlite_sequence WHERE name = '" + tableName + "'", null);
            if (cursor == null) {
                return 0;
            }
            if (cursor.moveToNext()) {
                return cursor.getLong(cursor.getColumnIndex("seq")) + 1;
            }
            cursor.close();
            cursor = database.rawQuery("SELECT COUNT(*) AS size FROM " + tableName, null);
            if (cursor == null || !cursor.moveToNext()) {
                return 0;
            }
            return cursor.getLong(cursor.getColumnIndex("size")) + 1;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    public static SensorManager.MeasurementConfigurationProvider importMeasurementConfigurationProvider(long providerId) {
        if (database == null || providerId <= 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Map<ID, Configuration<?>> configurationMap = new HashMap<>();
        //查找provider_id=providerId的所有传感器配置
        importSensorInfoConfigurations(providerId, builder, configurationMap);
        importMeasurementConfigurations(providerId, builder, configurationMap);
        return new CommonValueContainerConfigurationProvider(configurationMap);
    }

    private static void importSensorInfoConfigurations(
            long providerId, StringBuilder builder,
            Map<ID, Configuration<?>> configurationMap) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_CUSTOM_NAME)
                    .append(" FROM ").append(TABLE_SENSOR_CONFIGURATION)
                    .append(" WHERE ").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(" = ").append(providerId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor == null) {
                return;
            }
            int addressIndex = cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS);
            int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
            while (cursor.moveToNext()) {
                String customName = cursor.getString(customNameIndex);
                SensorInfoConfiguration configuration = new SensorInfoConfiguration();
                if (!TextUtils.isEmpty(customName)) {
                    configuration.setDecorator(new CommonSensorInfoDecorator(customName));
                }
                configurationMap.put(new ID(cursor.getInt(addressIndex)), configuration);
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void importMeasurementConfigurations(
            long providerId, StringBuilder builder,
            Map<ID, Configuration<?>> configurationMap) {
        importMeasurementConfigurationsWithSingleRangeWarner(providerId, builder, configurationMap);
        importMeasurementConfigurationsWithSwitchWarner(providerId, builder, configurationMap);
        importMeasurementConfigurationsWithoutWarner(builder, configurationMap);
    }

    private static void importMeasurementConfigurationsWithSingleRangeWarner(
            long providerId,
            StringBuilder builder,
            Map<ID, Configuration<?>> configurationMap) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ")
                    .append("m.").append(COLUMN_COMMON_ID)
                    .append(",m.").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",m.").append(COLUMN_CUSTOM_NAME)
                    .append(",m.").append(COLUMN_TYPE)
                    .append(',').append(COLUMN_LOW_LIMIT)
                    .append(',').append(COLUMN_HIGH_LIMIT)
                    .append(" FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" m,").append(TABLE_GENERAL_SINGLE_RANGE_WARNER)
                    .append(" gsr WHERE m.").append(COLUMN_COMMON_ID)
                    .append(" = gsr.").append(COLUMN_COMMON_ID)
                    .append(" AND ").append(COLUMN_SENSOR_CONFIGURATION_ID)
                    .append(" IN (SELECT s.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_SENSOR_CONFIGURATION)
                    .append(" s WHERE ").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(" = ").append(providerId).append(')');
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null) {
                int measurementConfigIdIndex = cursor.getColumnIndex(COLUMN_COMMON_ID);
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                int lowLimitIndex = cursor.getColumnIndex(COLUMN_LOW_LIMIT);
                int highLimitIndex = cursor.getColumnIndex(COLUMN_HIGH_LIMIT);
                int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
                while (cursor.moveToNext()) {
                    CommonSingleRangeWarner warner = new CommonSingleRangeWarner();
                    warner.setLowLimit(cursor.getDouble(lowLimitIndex));
                    warner.setHighLimit(cursor.getDouble(highLimitIndex));
                    importMeasurementConfiguration(configurationMap,
                            cursor, measurementIdIndex, customNameIndex,
                            typeIndex, measurementConfigIdIndex, warner);
                }
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void importMeasurementConfigurationsWithSwitchWarner(
            long providerId, StringBuilder builder,
            Map<ID, Configuration<?>> configurationMap) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ")
                    .append("m.").append(COLUMN_COMMON_ID)
                    .append(",m.").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",m.").append(COLUMN_CUSTOM_NAME)
                    .append(",m.").append(COLUMN_TYPE)
                    .append(',').append(COLUMN_ABNORMAL_VALUE)
                    .append(" FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" m,").append(TABLE_GENERAL_SWITCH_WARNER)
                    .append(" gs WHERE m.").append(COLUMN_COMMON_ID)
                    .append(" = gs.").append(COLUMN_COMMON_ID)
                    .append(" AND ").append(COLUMN_SENSOR_CONFIGURATION_ID)
                    .append(" IN (SELECT s.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_SENSOR_CONFIGURATION)
                    .append(" s WHERE ").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(" = ").append(providerId).append(')');
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null) {
                int measurementConfigIdIndex = cursor.getColumnIndex(COLUMN_COMMON_ID);
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                int abnormalValueIndex = cursor.getColumnIndex(COLUMN_ABNORMAL_VALUE);
                int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
                while (cursor.moveToNext()) {
                    CommonSwitchWarner warner = new CommonSwitchWarner();
                    warner.setAbnormalValue(cursor.getDouble(abnormalValueIndex));
                    importMeasurementConfiguration(configurationMap,
                            cursor, measurementIdIndex, customNameIndex,
                            typeIndex, measurementConfigIdIndex, warner);
                }
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void importMeasurementConfigurationsWithoutWarner(
            StringBuilder builder,
            Map<ID, Configuration<?>> configurationMap) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT DISTINCT ")
                    .append("m.").append(COLUMN_COMMON_ID)
                    .append(",m.").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_CUSTOM_NAME)
                    .append(",m.").append(COLUMN_TYPE)
                    .append(" FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" m WHERE m.").append(COLUMN_COMMON_ID)
                    .append(" NOT IN (SELECT gsr.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_GENERAL_SINGLE_RANGE_WARNER)
                    .append(" gsr UNION SELECT gs.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_GENERAL_SWITCH_WARNER)
                    .append(" gs)");
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null) {
                int measurementConfigIdIndex = cursor.getColumnIndex(COLUMN_COMMON_ID);
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
                while (cursor.moveToNext()) {
                    importMeasurementConfiguration(configurationMap,
                            cursor, measurementIdIndex, customNameIndex,
                            typeIndex, measurementConfigIdIndex,null);
                }
            }
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void importMeasurementConfiguration(
            Map<ID, Configuration<?>> configurationMap,
            Cursor cursor, int measurementIdIndex,
            int customNameIndex, int typeIndex,
            int measurementConfigIdIndex,
            Warner<DisplayMeasurement.Value> warner) {
        ID id = new ID(cursor.getLong(measurementIdIndex));
        DisplayMeasurementConfiguration configuration = buildMeasurementConfiguration(cursor, customNameIndex, typeIndex, measurementConfigIdIndex, warner);
        configurationMap.put(id, configuration);
    }

    @NonNull
    private static DisplayMeasurementConfiguration buildMeasurementConfiguration(Cursor cursor, int customNameIndex, int typeIndex, int measurementConfigIdIndex, Warner<DisplayMeasurement.Value> warner) {
        return buildMeasurementConfiguration(cursor, customNameIndex, typeIndex, cursor.getLong(measurementConfigIdIndex), warner);
    }

    @NonNull
    private static DisplayMeasurementConfiguration buildMeasurementConfiguration(Cursor cursor, int customNameIndex, int typeIndex, long measurementConfigId, Warner<DisplayMeasurement.Value> warner) {
        return buildMeasurementConfiguration(measurementConfigId, cursor.getInt(typeIndex), cursor.getString(customNameIndex), warner);
    }

//    @NonNull
//    private static DisplayMeasurementConfiguration buildMeasurementConfiguration(Cursor cursor, int customNameIndex, int typeIndex, Warner<DisplayMeasurement.Value> warner, long measurementConfigId) {
//        DisplayMeasurementConfiguration configuration = buildDisplayMeasurementConfigurationByType(cursor, typeIndex, measurementConfigId);
//        String customName = cursor.getString(customNameIndex);
//        if (!TextUtils.isEmpty(customName)) {
//            configuration.setDecorator(new CommonMeasurementDecorator(customName));
//        }
//        configuration.setWarner(warner);
//        return configuration;
//    }

    @NonNull
    private static DisplayMeasurementConfiguration buildMeasurementConfiguration(long measurementConfigId, int type, String customName, Warner<DisplayMeasurement.Value> warner) {
        DisplayMeasurementConfiguration configuration = buildDisplayMeasurementConfigurationByType(type, measurementConfigId);
        //String customName = cursor.getString(customNameIndex);
        if (!TextUtils.isEmpty(customName)) {
            configuration.setDecorator(new CommonMeasurementDecorator(customName));
        }
        configuration.setWarner(warner);
        return configuration;
    }

//    private static DisplayMeasurementConfiguration buildDisplayMeasurementConfigurationByType(Cursor cursor, int typeIndex, long measurementConfigId) {
//        String type = cursor.getString(typeIndex);
//        if (TextUtils.isEmpty(type)) {
//            return new DisplayMeasurementConfiguration();
//        } else {
//            switch (type) {
//                case COLUMN_TYPE:
//                    return buildRatchetWheelMeasurementConfiguration(measurementConfigId);
//                default:
//                    throw new IllegalArgumentException("abnormal type for measurement configuration");
//            }
//        }
//    }

    private static DisplayMeasurementConfiguration buildDisplayMeasurementConfigurationByType(int type, long measurementConfigId) {
        switch (type) {
            case SensorConfiguration.Measure.CT_NORMAL:
                return new DisplayMeasurementConfiguration();
            case SensorConfiguration.Measure.CT_RATCHET_WHEEL:
                return buildRatchetWheelMeasurementConfiguration(measurementConfigId);
            default:
                throw new IllegalArgumentException("abnormal type for measurement configuration");
        }
    }

    private static RatchetWheelMeasurementConfiguration buildRatchetWheelMeasurementConfiguration(long measurementConfigId) {
        RatchetWheelMeasurementConfiguration configuration = new RatchetWheelMeasurementConfiguration();
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ").append(COLUMN_INITIAL_DISTANCE)
                    .append(",").append(COLUMN_INITIAL_VALUE)
                    .append(" FROM ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append(" WHERE ").append(COLUMN_COMMON_ID)
                    .append(" = ").append(measurementConfigId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null) {
                int initialDistanceIndex = cursor.getColumnIndex(COLUMN_INITIAL_DISTANCE);
                int initialValueIndex = cursor.getColumnIndex(COLUMN_INITIAL_VALUE);
                while (cursor.moveToNext()) {
                    configuration.setInitialDistance(cursor.getDouble(initialDistanceIndex));
                    configuration.setInitialValue(cursor.getDouble(initialValueIndex));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return configuration;
    }

    public static boolean importDevicesWithNodes(long providerId, @NonNull OnImportDeviceListener listener) {
        if (database == null) {
            return false;
        }
        Cursor cDevice = null, cNode = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ").append(COLUMN_COMMON_ID)
                    .append(',').append(COLUMN_DEVICE_NAME)
                    .append(" FROM ").append(TABLE_DEVICE)
                    .append(" WHERE ").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(" = ").append(providerId);
            cDevice = database.rawQuery(builder.toString(), null);
            if (cDevice == null) {
                return false;
            }
            int idIndex = cDevice.getColumnIndex(COLUMN_COMMON_ID);
            int deviceNameIndex = cDevice.getColumnIndex(COLUMN_DEVICE_NAME);
            long id;
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_NODE_NAME)
                    .append(',').append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(" FROM ").append(TABLE_NODE)
                    .append(" WHERE ").append(COLUMN_DEVICE_ID)
                    .append(" = ");
            int prefixLength = builder.length();
            int nodeNameIndex, sensorIdIndex;
            String name;
            while (cDevice.moveToNext()) {
                id = cDevice.getLong(idIndex);
                builder.setLength(prefixLength);
                cNode = database.rawQuery(builder.append(id).toString(), null);
                if (cNode == null) {
                    return false;
                }
                nodeNameIndex = cNode.getColumnIndex(COLUMN_NODE_NAME);
                sensorIdIndex = cNode.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                List<Node> nodes = new ArrayList<>(cNode.getCount());
                Measurement measurement;
                while (cNode.moveToNext()) {
                    measurement = SensorManager.getMeasurement(cNode.getLong(sensorIdIndex));
                    if (measurement instanceof DisplayMeasurement) {
                        nodes.add(new Node(cNode.getString(nodeNameIndex),
                                (DisplayMeasurement<?>) measurement));
                    }
                }
                name = cDevice.getString(deviceNameIndex);
                cNode.close();
                cNode = null;
                listener.onImportDevice(new Device(TextUtils.isEmpty(name) ? "" : name, nodes));
            }
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cDevice != null) {
                cDevice.close();
            }
            if (cNode != null) {
                cNode.close();
            }
        }
        return false;
    }

    public static List<Device> importDevicesWithNodes(long providerId) {
        DeviceImporter importer = new DeviceImporter();
        if (importDevicesWithNodes(providerId, importer)) {
            return importer.getDevices();
        } else {
            return null;
        }
    }

    public interface OnImportDeviceListener {
        void onImportDevice(@NonNull Device device);
    }

    private static class DeviceImporter implements OnImportDeviceListener {

        private final List<Device> mDevices = new ArrayList<>();

        @Override
        public void onImportDevice(@NonNull Device device) {
            mDevices.add(device);
        }

        public List<Device> getDevices() {
            return Collections.unmodifiableList(mDevices);
        }
    }

    public static Cursor importDevices(long providerId) {
        if (database == null) {
            return null;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ").append(COLUMN_COMMON_ID)
                    .append(',').append(COLUMN_DEVICE_NAME)
                    .append(" FROM ").append(TABLE_DEVICE)
                    .append(" WHERE ").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(" = ").append(providerId)
                    .append(" ORDER BY ").append(COLUMN_DEVICE_NAME)
                    .append(" ASC");
            return database.rawQuery(builder.toString(), null);
        } catch (Exception e) {
            ExceptionLog.record(e);
        }
        return null;
    }

    public static Cursor importNodes(long deviceId) {
        if (database == null) {
            return null;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ").append(COLUMN_COMMON_ID)
                    .append(',').append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_NODE_NAME)
                    .append(" FROM ").append(TABLE_NODE)
                    .append(" WHERE ").append(COLUMN_DEVICE_ID)
                    .append(" = ").append(deviceId)
                    .append(" ORDER BY ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(" ASC");
            return database.rawQuery(builder.toString(), null);
        } catch (Exception e) {
            ExceptionLog.record(e);
        }
        return null;
    }

    public static Cursor importValueContainerConfigurationProviders() {
        if (database == null) {
            return null;
        }
        try {
            return database.query(TABLE_CONFIGURATION_PROVIDER,
                    new String[] { COLUMN_COMMON_ID, COLUMN_CONFIGURATION_PROVIDER_NAME },
                    null, null, null, null,
                    COLUMN_COMMON_ID + " ASC");
        } catch (Exception e) {
            ExceptionLog.record(e);
        }
        return null;
    }

    public static int insertValueContainerConfigurationProviderFromXml(String filePath) {
        if (database == null) {
            return 0;
        }
        try {
            setEnableForeignKeyConstraints(false);
            database.beginTransaction();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ValueContainerConfigurationProviderHandler handler = new ValueContainerConfigurationProviderHandler(database);
            parser.parse(new File(filePath), handler);
            database.setTransactionSuccessful();
            return handler.getProviderCount();
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            database.endTransaction();
            setEnableForeignKeyConstraints(true);
        }
        return 0;
    }

    //public static boolean exportValueContainerConfigurationProviderToXml(int )

    public static Cursor importSensorsConfiguration(long providerId) {
        if (database == null) {
            return null;
        }
        try {
            return database.query(TABLE_SENSOR_CONFIGURATION,
                    new String[] { COLUMN_COMMON_ID, COLUMN_SENSOR_ADDRESS, COLUMN_CUSTOM_NAME },
                    COLUMN_CONFIGURATION_PROVIDER_ID + " = ?",
                    new String[] { String.valueOf(providerId) },
                    null, null, COLUMN_SENSOR_ADDRESS + " ASC");
        } catch (Exception e) {
            ExceptionLog.record(e);
        }
        return null;
    }

    public static SimpleSQLiteAsyncEventHandler buildAsyncEventHandler(SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener listener) {
        return new SimpleSQLiteAsyncEventHandler(new SQLiteResolverDelegate(database), listener);
    }

    public static SensorConfiguration importSensorConfiguration(long sensorConfigId) {
        if (database == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ")
                    .append(COLUMN_SENSOR_ADDRESS).append(',')
                    .append(COLUMN_CUSTOM_NAME)
                    .append(" FROM ").append(TABLE_SENSOR_CONFIGURATION)
                    .append(" WHERE ").append(COLUMN_COMMON_ID)
                    .append(" = ").append(sensorConfigId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor == null || !cursor.moveToNext()) {
                return null;
            }
            int address = cursor.getInt(cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS));
            //PhysicalSensor sensor = SensorManager.getPhysicalSensor(address);
            SensorConfiguration sensorConfiguration = new SensorConfiguration(address);
            String customName = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOM_NAME));
            SensorInfoConfiguration infoConfiguration = new SensorInfoConfiguration();
            if (!TextUtils.isEmpty(customName)) {
                infoConfiguration.setDecorator(new CommonSensorInfoDecorator(customName));
            }
            sensorConfiguration.setBaseConfiguration(infoConfiguration);
            cursor.close();
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_COMMON_ID)
                    .append(",").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",").append(COLUMN_CUSTOM_NAME)
                    .append(",").append(COLUMN_TYPE)
                    .append(" FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" WHERE ").append(COLUMN_SENSOR_CONFIGURATION_ID)
                    .append(" = ").append(sensorConfigId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor == null) {
                return sensorConfiguration;
            }
            int idIndex = cursor.getColumnIndex(COLUMN_COMMON_ID);
            int valueIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
            int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
            int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
            long measurementConfigId;
            long measurementId;
            Warner<DisplayMeasurement.Value> warner;
            DisplayMeasurement.Configuration measurementConfiguration;
            while (cursor.moveToNext()) {
                measurementConfigId = cursor.getLong(idIndex);
                measurementId = cursor.getLong(valueIdIndex);
                warner = importMeasurementWarner(measurementConfigId, builder);
                measurementConfiguration = buildMeasurementConfiguration(cursor, customNameIndex, typeIndex, measurementConfigId, warner);
                sensorConfiguration.setMeasureConfiguration(measurementId, measurementConfigId, measurementConfiguration);
            }
            return sensorConfiguration;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static Warner<DisplayMeasurement.Value> importMeasurementWarner(long measurementConfigId, StringBuilder builder) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_LOW_LIMIT)
                    .append(',').append(COLUMN_HIGH_LIMIT)
                    .append(" FROM ").append(TABLE_GENERAL_SINGLE_RANGE_WARNER)
                    .append(" WHERE ").append(COLUMN_COMMON_ID)
                    .append(" = ").append(measurementConfigId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null && cursor.moveToNext()) {
                CommonSingleRangeWarner warner = new CommonSingleRangeWarner();
                warner.setHighLimit(cursor.getDouble(cursor.getColumnIndex(COLUMN_HIGH_LIMIT)));
                warner.setLowLimit(cursor.getDouble(cursor.getColumnIndex(COLUMN_LOW_LIMIT)));
                return warner;
            }
            if (cursor != null) {
                cursor.close();
            }
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_ABNORMAL_VALUE)
                    .append(" FROM ").append(TABLE_GENERAL_SWITCH_WARNER)
                    .append(" WHERE ").append(COLUMN_COMMON_ID)
                    .append(" = ").append(measurementConfigId);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null && cursor.moveToNext()) {
                CommonSwitchWarner warner = new CommonSwitchWarner();
                warner.setAbnormalValue(cursor.getDouble(cursor.getColumnIndex(COLUMN_ABNORMAL_VALUE)));
                return warner;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static boolean exportSensorDataToExcel(String directoryPath) {
        if (database == null || TextUtils.isEmpty(directoryPath)) {
            return false;
        }
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",m.").append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_RAW_VALUE)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(" FROM ").append(TABLE_SENSOR_DATA)
                    .append(" s,").append(TABLE_MEASUREMENT_DATA)
                    .append(" m WHERE (").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(">>32)=").append(COLUMN_SENSOR_ADDRESS)
                    .append(" AND s.").append(COLUMN_TIMESTAMP)
                    .append("=m.").append(COLUMN_TIMESTAMP);
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor == null) {
                return false;
            }
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdir();
            }
            WritableWorkbook book = Workbook.createWorkbook(new File(directory.getAbsolutePath() + File.separator + "data.xls"));
            WritableSheet sheet = book.createSheet("传感器数据", 0);
            sheet.addCell(new Label(0, 0, "地址"));
            sheet.addCell(new Label(1, 0, "数据类型值"));
            sheet.addCell(new Label(2, 0, "数据类型"));
            sheet.addCell(new Label(3, 0, "时间"));
            sheet.addCell(new Label(4, 0, "数据值"));
            sheet.addCell(new Label(5, 0, "电源电压"));
            int idIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
            int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
            int valueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
            int voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            int row = 1;
            long id;
            int address;
            PracticalMeasurement.DataType dataType;
            while (cursor.moveToNext()) {
                id = cursor.getLong(idIndex);
                address = ID.getAddress(id);
                dataType = SensorManager.getDataType(address, ID.getDataTypeValue(id), true);
                sheet.addCell(new Label(0, row, ID.getFormatAddress(address)));
                sheet.addCell(new Label(1, row, ID.getFormattedDataTypeValue(id)));
                sheet.addCell(new Label(2, row, dataType.getName()));
                date.setTime(cursor.getLong(timestampIndex));
                sheet.addCell(new Label(3, row, dateFormat.format(date)));
                sheet.addCell(new Label(4, row, dataType.formatValue(cursor.getDouble(valueIndex))));
                sheet.addCell(new Label(5, row, String.format("%.2fV", cursor.getFloat(voltageIndex))));
                ++row;
            }
            book.write();
            book.close();
            return true;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static long getLatestSensorHistoryDataTimestamp(@NonNull Sensor sensor) {
        long result = 0L;
        if (database != null) {
            Cursor cursor = null;
            String columnLatestTimestamp = "latest_time";
            try {
                StringBuilder builder = new StringBuilder();
                String table;
                if (sensor.getId().isPhysicalSensor()) {
                    table = TABLE_SENSOR_DATA;
                    builder.append(COLUMN_SENSOR_ADDRESS).append('=')
                            .append(sensor.getId().getAddress());
                } else {
                    table = TABLE_MEASUREMENT_DATA;
                    builder.append(COLUMN_MEASUREMENT_VALUE_ID).append('=')
                            .append(sensor.getMainMeasurement().getId().getId());
                }
                String condition = builder.toString();
                builder.setLength(0);
                builder.append("SELECT MAX(").append(COLUMN_TIMESTAMP)
                        .append(") ").append(columnLatestTimestamp)
                        .append(" FROM ").append(table)
                        .append(" WHERE ").append(condition);
                cursor = database.rawQuery(builder.toString(), null);
            } catch (Exception e) {
                ExceptionLog.record(e);
            } finally {
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        result = cursor.getLong(cursor.getColumnIndex(columnLatestTimestamp));
                        //Log.d(Tag.LOG_TAG_D_TEST, "timestamp: " + result + ", date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(result)));
                    }
                    cursor.close();
                }
            }
        }
        return result;
    }

    public interface SensorDataProvider {
        //通过getSensorDataState或者getMeasurementDataState返回
        //传感器或者测量量首条需要保存的历史数据，
        //但并不意味着其在数据库中也是首条，
        //所以需查询TABLE_SENSOR_INIT_DATA或者TABLE_MEASUREMENT_INIT_DATA
        //予以确认
        int FIRST_DATA = 1;
        //普通数据，存放于TABLE_SENSOR_DATA和TABLE_MEASUREMENT_DATA
        int NORMAL_DATA = 2;
        //根据配置规则确定的重复数据，无需保存
        int DUPLICATE_DATA = 3;
        int getSensorDataCount();
        SensorData provideSensorData();
        int getSensorDataState(SensorData data);
    }

    private static class SQLiteLauncher extends SQLiteOpenHelper {

        public SQLiteLauncher(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder builder = new StringBuilder();
            createSensorInitDataTable(db, builder);
            createMeasurementInitDataTable(db, builder);
            createSensorDataTable(db, builder);
            createMeasurementDataTable(db, builder);
            createConfigurationProviderDataTable(db, builder);
            createSensorConfigurationDataTable(db, builder);
            createMeasurementConfigurationDataTable(db, builder);
            createGeneralSingleRangeWarnerDataTable(db, builder);
            createGeneralSwitchWarnerDataTable(db, builder);
            createDeviceDataTable(db, builder);
            createNodeDataTable(db, builder);
            createRatchetWheelMeasurementConfigurationDataTable(db, builder);
        }

        private void createSensorInitDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_SENSOR_INIT_DATA).append(" (")
                    .append(COLUMN_SENSOR_ADDRESS).append(" INT NOT NULL PRIMARY KEY,")
                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
                    .append(COLUMN_BATTER_VOLTAGE).append(" FLOAT")
                    .append(')').toString());
        }

        private void createMeasurementInitDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_MEASUREMENT_INIT_DATA).append(" (")
                    .append(COLUMN_MEASUREMENT_VALUE_ID).append(" BIGINT NOT NULL PRIMARY KEY,")
                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
                    .append(COLUMN_RAW_VALUE).append(" DOUBLE NOT NULL")
                    .append(')').toString());
        }

        private void createSensorDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_SENSOR_DATA).append(" (")
                    .append(COLUMN_SENSOR_ADDRESS).append(" INT NOT NULL,")
                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
                    .append(COLUMN_BATTER_VOLTAGE).append(" FLOAT,")
                    .append(" PRIMARY KEY(").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_TIMESTAMP).append(')')
                    .append(')').toString());
        }

        private void createMeasurementDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_MEASUREMENT_DATA).append(" (")
                    .append(COLUMN_MEASUREMENT_VALUE_ID).append(" BIGINT NOT NULL,")
                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
                    .append(COLUMN_RAW_VALUE).append(" DOUBLE NOT NULL,")
                    .append(" PRIMARY KEY(").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_TIMESTAMP).append(')')
                    .append(')').toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion <= oldVersion) {
                return;
            }
            DatabaseUpdater updater = FlavorClassBuilder.buildImplementation(DatabaseUpdater.class);
            StringBuilder builder = new StringBuilder();
            if (oldVersion < VN_CONFIGURATION) {
                createConfigurationProviderDataTable(db, builder);
                createSensorConfigurationDataTable(db, builder);
                createMeasurementConfigurationDataTable(db, builder);
                createGeneralSingleRangeWarnerDataTable(db, builder);
                createGeneralSwitchWarnerDataTable(db, builder);
            }
            if (oldVersion < VN_DEVICE_NODE) {
                createDeviceDataTable(db, builder);
                createNodeDataTable(db, builder);
            }
            if (oldVersion < VN_BROKEN_TIME) {
                if (updater != null) {
                    updater.onVersionUpdateToBrokenTime(db, builder);
                }
            }
            if (oldVersion < VN_VIRTUAL_CONFIG) {
                addColumnTypeForMeasurementConfiguration(db, builder);
                createRatchetWheelMeasurementConfigurationDataTable(db, builder);
            }
            if (oldVersion < VN_GOOD_TYPE) {
                modifyMeasurementConfigurationColumnTypeDataType(db, builder);
            }
            if (isTableRatchetWheelConfigurationExits(db, builder)) {
                if (oldVersion < VN_THIN_RATCHET_WHEEL) {
                    deleteColumnCustomNameFromTableRatchetWheelConfiguration(db, builder);
                }
            } else {
                createRatchetWheelMeasurementConfigurationDataTable(db, builder);
            }
        }

        private void createConfigurationProviderDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_CONFIGURATION_PROVIDER).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_CONFIGURATION_PROVIDER_NAME).append(" VARCHAR(255) NOT NULL,")
                    .append(COLUMN_CREATE_TIME).append(" BIGINT NOT NULL,")
                    .append(COLUMN_MODIFY_TIME).append(" BIGINT NOT NULL")
                    .append(')').toString());
        }

        private void createSensorConfigurationDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_SENSOR_CONFIGURATION).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_CONFIGURATION_PROVIDER_ID).append(" INTEGER NOT NULL,")
                    .append(COLUMN_SENSOR_ADDRESS).append(" INT NOT NULL,")
                    .append(COLUMN_CUSTOM_NAME).append(" VARCHAR(255), ")
                    .append("UNIQUE(").append(COLUMN_CONFIGURATION_PROVIDER_ID)
                    .append(", ").append(COLUMN_SENSOR_ADDRESS).append("), ")
                    .append("FOREIGN KEY(").append(COLUMN_CONFIGURATION_PROVIDER_ID).append(") ")
                    .append("REFERENCES ").append(TABLE_CONFIGURATION_PROVIDER)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void createMeasurementConfigurationDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_MEASUREMENT_CONFIGURATION).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_SENSOR_CONFIGURATION_ID).append(" INTEGER NOT NULL,")
                    .append(COLUMN_MEASUREMENT_VALUE_ID).append(" BIGINT NOT NULL,")
                    .append(COLUMN_CUSTOM_NAME).append(" VARCHAR(255),")
                    .append(COLUMN_TYPE).append(" INT,")   //版本VN_VIRTUAL_CONFIG添加，版本VN_GOOD_TYPE将类型从VARCHAR(255)改为INT
                    .append("UNIQUE(").append(COLUMN_SENSOR_CONFIGURATION_ID)
                    .append(", ").append(COLUMN_MEASUREMENT_VALUE_ID).append("),")
                    .append("FOREIGN KEY(").append(COLUMN_SENSOR_CONFIGURATION_ID)
                    .append(") REFERENCES ").append(TABLE_SENSOR_CONFIGURATION)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void createGeneralSingleRangeWarnerDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_GENERAL_SINGLE_RANGE_WARNER).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER NOT NULL PRIMARY KEY,")
                    .append(COLUMN_LOW_LIMIT).append(" DOUBLE NOT NULL,")
                    .append(COLUMN_HIGH_LIMIT).append(" DOUBLE NOT NULL,")
                    .append("FOREIGN KEY(").append(COLUMN_COMMON_ID).append(") ")
                    .append("REFERENCES ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE,")
                    .append("CHECK(").append(COLUMN_LOW_LIMIT)
                    .append(" < ").append(COLUMN_HIGH_LIMIT).append(')')
                    .append(')').toString());
        }

        private void createGeneralSwitchWarnerDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_GENERAL_SWITCH_WARNER).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER NOT NULL PRIMARY KEY,")
                    .append(COLUMN_ABNORMAL_VALUE).append(" DOUBLE NOT NULL,")
                    .append("FOREIGN KEY(").append(COLUMN_COMMON_ID).append(") ")
                    .append("REFERENCES ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void createDeviceDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_DEVICE).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_CONFIGURATION_PROVIDER_ID).append(" INTEGER NOT NULL,")
                    .append(COLUMN_DEVICE_NAME).append(" VARCHAR(255), ")
                    .append("FOREIGN KEY(").append(COLUMN_CONFIGURATION_PROVIDER_ID).append(") ")
                    .append("REFERENCES ").append(TABLE_CONFIGURATION_PROVIDER)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void createNodeDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_NODE).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_DEVICE_ID).append(" INTEGER NOT NULL,")
                    .append(COLUMN_MEASUREMENT_VALUE_ID).append(" BIGINT NOT NULL,")
                    .append(COLUMN_NODE_NAME).append(" VARCHAR(255),")
                    .append("UNIQUE(").append(COLUMN_DEVICE_ID)
                    .append(", ").append(COLUMN_MEASUREMENT_VALUE_ID).append("),")
                    .append("FOREIGN KEY(").append(COLUMN_DEVICE_ID)
                    .append(") REFERENCES ").append(TABLE_DEVICE)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void addColumnTypeForMeasurementConfiguration(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("ALTER TABLE ")
                    .append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" ADD COLUMN ")
                    .append(COLUMN_TYPE)
                    .append(" VARCHAR(255)")
                    .toString());
        }

        private void createRatchetWheelMeasurementConfigurationDataTable(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            db.execSQL(builder.append("CREATE TABLE ")
                    .append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION).append(" (")
                    .append(COLUMN_COMMON_ID).append(" INTEGER NOT NULL PRIMARY KEY,")
                    .append(COLUMN_INITIAL_DISTANCE).append(" DOUBLE NOT NULL,")
                    .append(COLUMN_INITIAL_VALUE).append(" DOUBLE NOT NULL,")
                    //.append(COLUMN_CUSTOM_NAME).append(" VARCHAR(255),")  //版本VN_THIN_RATCHET_WHEEL移除
                    .append("FOREIGN KEY(").append(COLUMN_COMMON_ID).append(") ")
                    .append("REFERENCES ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append('(').append(COLUMN_COMMON_ID).append(") ON DELETE CASCADE")
                    .append(')').toString());
        }

        private void modifyMeasurementConfigurationColumnTypeDataType(SQLiteDatabase db, StringBuilder builder) {
            //把原表改成另外一个名字作为暂存表
            builder.setLength(0);
            builder.append("ALTER TABLE ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" RENAME TO ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
            //用原表的名字创建新表
            createMeasurementConfigurationDataTable(db, builder);
            //将暂存表数据写入到新表，很方便的是不需要去理会自动增长的 ID
            builder.setLength(0);
            builder.append("INSERT INTO ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" SELECT * FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
            //删除暂存表
            builder.setLength(0);
            builder.append("DROP TABLE ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
        }

        private void deleteColumnCustomNameFromTableRatchetWheelConfiguration(SQLiteDatabase db, StringBuilder builder) {
            //把原表改成另外一个名字作为暂存表
            builder.setLength(0);
            builder.append("ALTER TABLE ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append(" RENAME TO ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
            //用原表的名字创建新表
            createRatchetWheelMeasurementConfigurationDataTable(db, builder);
            //将暂存表数据写入到新表，很方便的是不需要去理会自动增长的 ID
            builder.setLength(0);
            builder.append("INSERT INTO ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append(" SELECT ").append(COLUMN_COMMON_ID)
                    .append(',').append(COLUMN_INITIAL_VALUE)
                    .append(',').append(COLUMN_INITIAL_DISTANCE)
                    .append(" FROM ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
            //删除暂存表
            builder.setLength(0);
            builder.append("DROP TABLE ").append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append("_tmp");
            db.execSQL(builder.toString());
        }

        private boolean isTableRatchetWheelConfigurationExits(SQLiteDatabase db, StringBuilder builder) {
            builder.setLength(0);
            builder.append("SELECT name FROM sqlite_master WHERE name = '")
                    .append(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION)
                    .append("'");
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(builder.toString(), null);
                if (cursor != null && cursor.moveToNext()) {
                    return true;
                }
            } catch (Exception e) {
                ExceptionLog.record(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return false;
        }
    }

    private static class ValueContainerConfigurationProviderHandler extends DefaultHandler {

        //private SQLiteDatabase mDatabase;
        private long mProviderConfigId;
        private long mSensorConfigId;
        private long mMeasurementConfigId;
        private long mDeviceId;
        //private long mNodeId;
        private StringBuilder mBuilder;
        private ContentValues mValues;
        private int mAddress;
        private String mSensorName;
        private String mMeasurementName;
        private boolean mIntoMeasurementElement;
        private int mMeasurementType;
        private long mMeasurementValueId;
        //private String mWarnerTableName;
        private double mAbnormalValue;
        private double mLowLimit;
        private double mHighLimit;
        private String mWarnerType;
        private String mProviderName;
        private int mProviderCount;
        private double mInitValue;
        private double mInitDistance;

        public ValueContainerConfigurationProviderHandler(SQLiteDatabase database) {
            //mDatabase = database;
        }

        public int getProviderCount() {
            return mProviderCount;
        }

        @Override
        public void startDocument() {
            mBuilder = new StringBuilder();
            mValues = new ContentValues();
            mProviderConfigId = getNextAutoIncrementId(TABLE_CONFIGURATION_PROVIDER);
            if (mProviderConfigId == 0) {
                throw new SqlExecuteFailed("provider id get failed");
            }
            mSensorConfigId = getNextAutoIncrementId(TABLE_SENSOR_CONFIGURATION);
            if (mSensorConfigId == 0) {
                throw new SqlExecuteFailed("measurement id get failed");
            }
            mMeasurementConfigId = getNextAutoIncrementId(TABLE_MEASUREMENT_CONFIGURATION);
            if (mMeasurementConfigId == 0) {
                throw new SqlExecuteFailed("measurement id get failed");
            }
            mDeviceId = getNextAutoIncrementId(TABLE_DEVICE);
            if (mDeviceId == 0) {
                throw new SqlExecuteFailed("device id get failed");
            }
            mMeasurementType = SensorConfiguration.Measure.CT_NORMAL;
            mInitValue = 0.0;
            mInitDistance = 0.0;
//            mNodeId = getNextAutoIncrementId(TABLE_NODE);
//            if (mNodeId == 0) {
//                throw new SqlExecuteFailed("node id get failed");
//            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (localName) {
                case "provider":
                    mProviderName = attributes.getValue(TAG_NAME);
                    break;
                case TAG_SENSOR:
                    mAddress = Integer.parseInt(attributes.getValue(TAG_ADDRESS), 16);
                    break;
                case TAG_MEASUREMENT: {
                    mIntoMeasurementElement = true;
                    String dataType = attributes.getValue(TAG_TYPE);
                    String index = attributes.getValue(TAG_INDEX);
                    String measurementType = attributes.getValue("pattern");
                    mMeasurementValueId = ID.getId(mAddress,
                            TextUtils.isEmpty(dataType) ? 0 : (byte) Integer.parseInt(dataType, 16),
                            TextUtils.isEmpty(index) ? 0 : Integer.parseInt(index));
                    mMeasurementType = TextUtils.isEmpty(measurementType) ? SensorConfiguration.Measure.CT_NORMAL : Integer.parseInt(measurementType);
                } break;
                case TAG_WARNER:
                    mWarnerType = attributes.getValue(TAG_TYPE);
                    break;
                case TAG_DEVICE:
                    mValues.clear();
                    mValues.put(COLUMN_CONFIGURATION_PROVIDER_ID, mProviderConfigId);
                    String deviceName = attributes.getValue(TAG_NAME);
                    if (TextUtils.isEmpty(deviceName)) {
                        throw new NullPointerException("device name may not be empty");
                    }
                    mValues.put(COLUMN_DEVICE_NAME, deviceName);
                    if (database.insert(TABLE_DEVICE, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert device failed");
                    }
                    break;
                case TAG_NODE:
                    mValues.clear();
                    mValues.put(COLUMN_DEVICE_ID, mDeviceId);
                    String dataType = attributes.getValue(TAG_TYPE);
                    String index = attributes.getValue(TAG_INDEX);
                    long measurementValueId = ID.getId(Integer.parseInt(attributes.getValue(TAG_ADDRESS), 16),
                            TextUtils.isEmpty(dataType) ? 0 : (byte) Integer.parseInt(dataType, 16),
                            TextUtils.isEmpty(index) ? 0 : Integer.parseInt(index));
                    if (measurementValueId == 0) {
                        throw new LackParameterException("measurement address and measurement type may not be empty");
                    }
                    mValues.put(COLUMN_MEASUREMENT_VALUE_ID, measurementValueId);
                    String nodeName = attributes.getValue(TAG_NAME);
                    if (!TextUtils.isEmpty(nodeName)) {
                        //throw new NullPointerException("node name may not be empty");
                        mValues.put(COLUMN_NODE_NAME, nodeName);
                    }
                    if (database.insert(TABLE_NODE, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert node failed");
                    }
                    //++mNodeId;
                    break;
            }
            mBuilder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            mBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (localName) {
                case TAG_NAME:
                    if (mIntoMeasurementElement) {
                        mMeasurementName = mBuilder.toString();
                    } else {
                        mSensorName = mBuilder.toString();
                    }
                    break;
                case "abnormal":
                    mAbnormalValue = Double.parseDouble(mBuilder.toString());
                    break;
                case "low":
                    mLowLimit = Double.parseDouble(mBuilder.toString());
                    break;
                case "high":
                    mHighLimit = Double.parseDouble(mBuilder.toString());
                    break;
                case TAG_WARNER:
                    if (insertWarner() == -1) {
                        throw new SqlExecuteFailed("insert warner failed");
                    }
                    break;
                case TAG_MEASUREMENT:
                    mValues.clear();
                    mValues.put(COLUMN_SENSOR_CONFIGURATION_ID, mSensorConfigId);
                    if (mMeasurementValueId == 0) {
                        throw new LackParameterException("measurement address and measurement type may not be empty");
                    }
                    mValues.put(COLUMN_MEASUREMENT_VALUE_ID, mMeasurementValueId);
                    if (!TextUtils.isEmpty(mMeasurementName)) {
                        mValues.put(COLUMN_CUSTOM_NAME, mMeasurementName);
                    }
                    if (mMeasurementType != SensorConfiguration.Measure.CT_NORMAL) {
                        mValues.put(COLUMN_TYPE, mMeasurementType);
                    }
                    if (database.insert(TABLE_MEASUREMENT_CONFIGURATION, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert measurement configuration failed");
                    }
                    if (mMeasurementType != SensorConfiguration.Measure.CT_NORMAL) {
                        insertExtraMeasurementConfig();
                        mMeasurementType = SensorConfiguration.Measure.CT_NORMAL;
                    }
                    mMeasurementValueId = 0;
                    mMeasurementName = null;
                    ++mMeasurementConfigId;
                    mIntoMeasurementElement = false;
                    break;
                case TAG_SENSOR:
                    mValues.clear();
                    mValues.put(COLUMN_CONFIGURATION_PROVIDER_ID, mProviderConfigId);
                    if (mAddress == 0) {
                        throw new LackParameterException("measurement address may not be empty");
                    }
                    mValues.put(COLUMN_SENSOR_ADDRESS, mAddress);
                    if (!TextUtils.isEmpty(mSensorName)) {
                        mValues.put(COLUMN_CUSTOM_NAME, mSensorName);
                    }
                    if (database.insert(TABLE_SENSOR_CONFIGURATION, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert measurement configuration failed");
                    }
                    mAddress = 0;
                    mSensorName = null;
                    ++mSensorConfigId;
                    break;
                case "provider":
                    if (TextUtils.isEmpty(mProviderName)) {
                        throw new LackParameterException("provider name may not be empty");
                    }
                    mValues.clear();
                    mValues.put(COLUMN_CONFIGURATION_PROVIDER_NAME, mProviderName);
                    long currentTime = System.currentTimeMillis();
                    mValues.put(COLUMN_CREATE_TIME, currentTime);
                    mValues.put(COLUMN_MODIFY_TIME, currentTime);
                    if (database.insert(TABLE_CONFIGURATION_PROVIDER, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert configuration provider failed");
                    }
                    mProviderName = null;
                    ++mProviderConfigId;
                    ++mProviderCount;
                    break;
                case TAG_DEVICE:
                    ++mDeviceId;
                    break;
                case "InitValue":
                    mInitValue = Double.parseDouble(mBuilder.toString());
                    break;
                case "InitDistance":
                    mInitDistance = Double.parseDouble(mBuilder.toString());
                    break;
            }
        }

        private void insertExtraMeasurementConfig() {
            switch (mMeasurementType) {
                case SensorConfiguration.Measure.CT_RATCHET_WHEEL: {
                    mValues.clear();
                    mValues.put(COLUMN_COMMON_ID, mMeasurementConfigId);
                    mValues.put(COLUMN_INITIAL_VALUE, mInitValue);
                    mValues.put(COLUMN_INITIAL_DISTANCE, mInitDistance);
                    if (database.insert(TABLE_RATCHET_WHEEL_MEASUREMENT_CONFIGURATION, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert measurement extra configuration failed");
                    }
                    mInitValue = 0.0;
                    mInitDistance = 0.0;
                } break;
            }
        }

        private long insertWarner() {
            long result;
            mValues.clear();
            mValues.put(COLUMN_COMMON_ID, mMeasurementConfigId);
            switch (mWarnerType) {
                case "gs":
                    mValues.put(COLUMN_ABNORMAL_VALUE, mAbnormalValue);
                    result = database.insert(TABLE_GENERAL_SWITCH_WARNER, null, mValues);
                    mAbnormalValue = 0;
                    break;
                case "gsr":
                    mValues.put(COLUMN_LOW_LIMIT, mLowLimit);
                    mValues.put(COLUMN_HIGH_LIMIT, mHighLimit);
                    result = database.insert(TABLE_GENERAL_SINGLE_RANGE_WARNER, null, mValues);
                    mLowLimit = 0;
                    mHighLimit = 0;
                    break;
                default:
                    throw new LackParameterException("warner type may not be null");
            }
            return result;
        }

        public static class LackParameterException extends RuntimeException {

            private static final long serialVersionUID = -7972526641793081005L;

            public LackParameterException() {
                super();
            }

            public LackParameterException(String message) {
                super(message);
            }
        }

        public static class SqlExecuteFailed extends RuntimeException {

            private static final long serialVersionUID = 5217587140576239446L;

            public SqlExecuteFailed() {
                super();
            }

            public SqlExecuteFailed(String message) {
                super(message);
            }
        }
    }
}
