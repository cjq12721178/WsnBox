package com.weisi.tool.wsnbox.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.cjq.lib.weisi.node.Sensor;
import com.cjq.lib.weisi.node.SensorManager;
import com.cjq.lib.weisi.node.ValueContainer;
import com.cjq.tool.qbox.database.SQLiteResolverDelegate;
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.CommonValueContainerConfigurationProvider;
import com.weisi.tool.wsnbox.bean.configuration.MeasurementConfiguration;
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration;
import com.weisi.tool.wsnbox.bean.data.SensorData;
import com.weisi.tool.wsnbox.bean.decorator.CommonMeasurementDecorator;
import com.weisi.tool.wsnbox.bean.decorator.CommonSensorDecorator;
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner;
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner;
import com.weisi.tool.wsnbox.io.Constant;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by CJQ on 2017/11/9.
 */

public class SensorDatabase implements Constant {

    private static final String DATABASE_NAME = "SensorStorage.db";
    //数据库初始版本，存储传感器历史数据
    private static final int VN_WINDOW = 1;
    //增加对传感器配置的管理
    private static final int VN_CONFIGURATION = 2;
    private static final int CURRENT_VERSION_NO = VN_CONFIGURATION;

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

    public static boolean importSensorHistoryValues(int address,
                                                    long startTime,
                                                    long endTime,
                                                    int limitCount,
                                                    SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null || startTime > endTime) {
            return false;
        }
        Cursor cursor = null;
        try {
            StringBuffer buffer = new StringBuffer();
            //导入传感器历史数据
            buffer.append("SELECT ").append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(" FROM ").append(TABLE_SENSOR_DATA)
                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
                    .append(" = ").append(address);
            if (startTime > 0) {
                buffer.append(" AND ");
                if (endTime > 0) {
                    buffer.append('(').append(COLUMN_TIMESTAMP)
                            .append(" BETWEEN ").append(startTime)
                            .append(" AND ").append(endTime)
                            .append(')');
                } else {
                    buffer.append(COLUMN_TIMESTAMP)
                            .append(" >= ")
                            .append(startTime);
                }
            } else if (endTime > 0) {
                buffer.append(" AND ")
                        .append(COLUMN_TIMESTAMP)
                        .append(" < ")
                        .append(endTime);
            }
            buffer.append(" ORDER BY ").append(COLUMN_TIMESTAMP);
            if (limitCount > 0) {
                buffer.append(" LIMIT ").append(limitCount);
            }
            cursor = database.rawQuery(buffer.toString(), null);
            if (cursor == null) {
                return false;
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
            cursor.close();
            //导入传感器测量量历史数据
            buffer.setLength(0);
            buffer.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
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
            cursor = database.rawQuery(buffer.toString(), null);
            if (cursor == null) {
                return false;
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
    public static int getSensorWithHistoryValueCount() {
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

    public static boolean importSensorEarliestValue(SensorHistoryInfoReceiver receiver) {
        if (database == null || receiver == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            final int SELECT_LIMIT_COUNT = 10;
            StringBuffer buffer = new StringBuffer();
            //导入具有历史数据的传感器及其首条历史数据
            buffer.append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(" FROM ").append(TABLE_SENSOR_INIT_DATA);
            int prefixLength = buffer.length();
            int startPosition = 0, addressIndex = 0,
                    timestampIndex = 0, voltageIndex = 0;
            do {
                if (cursor != null) {
                    cursor.close();
                }
                buffer.setLength(prefixLength);
                cursor = database.rawQuery(buffer.append(" LIMIT ")
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
            //导入相应传感器测量量的首条历史数据
            buffer.setLength(0);
            buffer.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_RAW_VALUE)
                    .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA);
            prefixLength = buffer.length();
            startPosition = 0;
            int measurementIdIndex = 0, valueIndex = 0;
            do {
                if (cursor != null) {
                    cursor.close();
                }
                buffer.setLength(prefixLength);
                cursor = database.rawQuery(buffer.append(" LIMIT ")
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
            StringBuffer sensorDataBuffer = new StringBuffer();
            StringBuffer measurementDataBuffer = new StringBuffer();
            database.beginTransaction();
            sensorDataBuffer.append("INSERT OR IGNORE INTO ");
            measurementDataBuffer.append(sensorDataBuffer);
            int insertPrefixLen = sensorDataBuffer.length();
            sensorDataBuffer.append(TABLE_SENSOR_DATA)
                    .append(" (").append(COLUMN_SENSOR_ADDRESS)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_BATTER_VOLTAGE)
                    .append(") VALUES (");
            String sensorDataNormalInsertSentence = sensorDataBuffer.toString();
            int sensorDataNormalInsertWholePrefixLength = sensorDataBuffer.length();
            measurementDataBuffer.append(TABLE_MEASUREMENT_DATA)
                    .append(" (").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_TIMESTAMP)
                    .append(',').append(COLUMN_RAW_VALUE)
                    .append(") VALUES (");
            String measurementDataNormalInsertSentence = measurementDataBuffer.toString();
            int measurementDataNormalInsertWholePrefixLength = measurementDataBuffer.length();
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
                            sensorDataBuffer.replace(insertPrefixLen,
                                    insertPrefixLen + TABLE_SENSOR_INIT_DATA.length(),
                                    TABLE_SENSOR_DATA);
                            sensorDataInsertSentenceChanged = false;
                        }
                        if (measurementDataInsertSentenceChanged) {
                            measurementDataBuffer.replace(insertPrefixLen,
                                    insertPrefixLen + TABLE_MEASUREMENT_INIT_DATA.length(),
                                    TABLE_MEASUREMENT_DATA);
                            measurementDataInsertSentenceChanged = false;
                        }
                        sensorDataBuffer.setLength(sensorDataNormalInsertWholePrefixLength);
                        measurementDataBuffer.setLength(measurementDataNormalInsertWholePrefixLength);
                        break;
                    case SensorDataProvider.FIRST_DATA:
                        sensorDataBuffer.setLength(0);
                        cursor = database.rawQuery(sensorDataBuffer
                                .append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
                                .append(" FROM ").append(TABLE_SENSOR_INIT_DATA)
                                .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
                                .append(" = ").append(sensorData.getAddress())
                                .toString(), null);
                        sensorDataBuffer.setLength(0);
                        sensorDataBuffer.append(sensorDataNormalInsertSentence);
                        if (cursor == null
                                || !cursor.moveToNext()
                                || cursor.getInt(cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS)) != sensorData.getAddress()) {
                            sensorDataBuffer.replace(insertPrefixLen, insertPrefixLen + TABLE_SENSOR_DATA.length(), TABLE_SENSOR_INIT_DATA);
                            sensorDataInsertSentenceChanged = true;
                        } else {
                            sensorDataInsertSentenceChanged = false;
                        }
                        measurementDataBuffer.setLength(0);
                        if (cursor != null) {
                            cursor.close();
                        }
                        cursor = database.rawQuery(measurementDataBuffer
                                .append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                                .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA)
                                .append(" WHERE ").append(COLUMN_MEASUREMENT_VALUE_ID)
                                .append(" = ").append(sensorData.getId())
                                .toString(), null);
                        measurementDataBuffer.setLength(0);
                        measurementDataBuffer.append(measurementDataNormalInsertSentence);
                        if (cursor == null
                                || !cursor.moveToNext()
                                || cursor.getLong(cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID)) != sensorData.getId()) {
                            measurementDataBuffer.replace(insertPrefixLen, insertPrefixLen + TABLE_MEASUREMENT_DATA.length(), TABLE_MEASUREMENT_INIT_DATA);
                            measurementDataInsertSentenceChanged = true;
                        } else {
                            measurementDataInsertSentenceChanged = false;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        break;
                }
                database.execSQL(sensorDataBuffer.append(sensorData.getAddress())
                        .append(',').append(sensorData.getTimestamp())
                        .append(',').append(sensorData.getBatteryVoltage())
                        .append(')').toString());
                database.execSQL(measurementDataBuffer.append(sensorData.getId())
                        .append(',').append(sensorData.getTimestamp())
                        .append(',').append(sensorData.getRawValue())
                        .append(')').toString());
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

    public static SensorManager.ValueContainerConfigurationProvider importValueContainerConfigurationProvider(long providerId) {
        if (database == null || providerId <= 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        //查找provider_id=providerId的所有传感器配置
        Map<Integer, Sensor.Configuration> sensorConfigs = importSensorConfigurations(providerId, builder);
        if (sensorConfigs == null) {
            return null;
        }
        Map<Long, Sensor.Measurement.Configuration> measurementConfigs = importMeasurementConfigurations(providerId, builder);
        if (measurementConfigs == null) {
            return null;
        }
        return new CommonValueContainerConfigurationProvider(sensorConfigs, measurementConfigs);
    }

    private static Map<Integer, Sensor.Configuration> importSensorConfigurations(long providerId, StringBuilder builder) {
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
                return null;
            }
            Map<Integer, Sensor.Configuration> sensorConfigs = new HashMap<>();
            int addressIndex = cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS);
            int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
            while (cursor.moveToNext()) {
                String customName = cursor.getString(customNameIndex);
                SensorConfiguration configuration = new SensorConfiguration();
                if (!TextUtils.isEmpty(customName)) {
                    configuration.setDecorator(new CommonSensorDecorator(customName));
                }
                sensorConfigs.put(cursor.getInt(addressIndex), configuration);
            }
            return sensorConfigs;
        } catch (Exception e) {
            ExceptionLog.record(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static Map<Long, Sensor.Measurement.Configuration> importMeasurementConfigurations(long providerId, StringBuilder builder) {
        Map<Long, Sensor.Measurement.Configuration> measurementConfigs = new HashMap<>();
        importMeasurementConfigurationsWithSingleRangeWarner(providerId, builder, measurementConfigs);
        importMeasurementConfigurationsWithSwitchWarner(providerId, builder, measurementConfigs);
        importMeasurementConfigurationsWithoutWarner(builder, measurementConfigs);
        return measurementConfigs;
    }

    private static void importMeasurementConfigurationsWithSingleRangeWarner(
            long providerId,
            StringBuilder builder,
            Map<Long, Sensor.Measurement.Configuration> measurementConfigs) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",m.").append(COLUMN_CUSTOM_NAME)
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
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                int lowLimitIndex = cursor.getColumnIndex(COLUMN_LOW_LIMIT);
                int highLimitIndex = cursor.getColumnIndex(COLUMN_HIGH_LIMIT);
                while (cursor.moveToNext()) {
                    CommonSingleRangeWarner warner = new CommonSingleRangeWarner();
                    warner.setLowLimit(cursor.getDouble(lowLimitIndex));
                    warner.setHighLimit(cursor.getDouble(highLimitIndex));
                    importMeasurementConfiguration(measurementConfigs, cursor, measurementIdIndex, customNameIndex, warner);
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

    private static void importMeasurementConfigurationsWithSwitchWarner(long providerId, StringBuilder builder, Map<Long, Sensor.Measurement.Configuration> measurementConfigs) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(",m.").append(COLUMN_CUSTOM_NAME)
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
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                int abnormalValueIndex = cursor.getColumnIndex(COLUMN_ABNORMAL_VALUE);
                while (cursor.moveToNext()) {
                    CommonSwitchWarner warner = new CommonSwitchWarner();
                    warner.setAbnormalValue(cursor.getDouble(abnormalValueIndex));
                    importMeasurementConfiguration(measurementConfigs, cursor, measurementIdIndex, customNameIndex, warner);
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

    private static void importMeasurementConfigurationsWithoutWarner(StringBuilder builder, Map<Long, Sensor.Measurement.Configuration> measurementConfigs) {
        Cursor cursor = null;
        try {
            builder.setLength(0);
            builder.append("SELECT DISTINCT ").append(COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(COLUMN_CUSTOM_NAME)
                    .append(" FROM ").append(TABLE_MEASUREMENT_CONFIGURATION)
                    .append(" m WHERE m.").append(COLUMN_COMMON_ID)
                    .append(" NOT IN (SELECT gsr.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_GENERAL_SINGLE_RANGE_WARNER)
                    .append(" gsr UNION SELECT gs.").append(COLUMN_COMMON_ID)
                    .append(" FROM ").append(TABLE_GENERAL_SWITCH_WARNER)
                    .append(" gs)");
            cursor = database.rawQuery(builder.toString(), null);
            if (cursor != null) {
                int measurementIdIndex = cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID);
                int customNameIndex = cursor.getColumnIndex(COLUMN_CUSTOM_NAME);
                while (cursor.moveToNext()) {
                    importMeasurementConfiguration(measurementConfigs, cursor, measurementIdIndex, customNameIndex, null);
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

    private static void importMeasurementConfiguration(Map<Long, Sensor.Measurement.Configuration> measurementConfigs,
                                                       Cursor cursor,
                                                       int measurementIdIndex,
                                                       int customNameIndex,
                                                       ValueContainer.Warner<Sensor.Measurement.Value> warner) {
        MeasurementConfiguration configuration = new MeasurementConfiguration();
        String customName = cursor.getString(customNameIndex);
        if (!TextUtils.isEmpty(customName)) {
            configuration.setDecorator(new CommonMeasurementDecorator(customName));
        }
        //warner = new CommonSingleRangeWarner();
        configuration.setWarner(warner);
        measurementConfigs.put(cursor.getLong(measurementIdIndex), configuration);
    }

    public static Cursor importValueContainerConfigurationProviders() {
        if (database == null) {
            return null;
        }
        try {
            return database.query(TABLE_CONFIGURATION_PROVIDER,
                    new String[] { COLUMN_COMMON_ID, COLUMN_CONFIGURATION_PROVIDER_NAME},
                    null, null, null, null, null);
        } catch (Exception e) {
            ExceptionLog.record(e);
        }
        return null;
    }

    public static int insertValueContainerConfigurationProviderFromXml(Context context, String filePath) {
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

    public static SimpleSQLiteAsyncEventHandler buildAsyncEventHandler(SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener listener) {
        return new SimpleSQLiteAsyncEventHandler(new SQLiteResolverDelegate(database), listener);
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
            if (oldVersion < VN_CONFIGURATION) {
                StringBuilder builder = new StringBuilder();
                createConfigurationProviderDataTable(db, builder);
                createSensorConfigurationDataTable(db, builder);
                createMeasurementConfigurationDataTable(db, builder);
                createGeneralSingleRangeWarnerDataTable(db, builder);
                createGeneralSwitchWarnerDataTable(db, builder);
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
    }

    private static class ValueContainerConfigurationProviderHandler extends DefaultHandler {

        private SQLiteDatabase mDatabase;
        private long mProviderConfigId;
        private long mSensorConfigId;
        private long mMeasurementConfigId;
        private StringBuilder mBuilder;
        private ContentValues mValues;
        private int mAddress;
        private String mSensorName;
        private String mMeasurementName;
        private boolean mIntoMeasurementElement;
        private long mMeasurementValueId;
        //private String mWarnerTableName;
        private double mAbnormalValue;
        private double mLowLimit;
        private double mHighLimit;
        private String mWarnerType;
        private String mProviderName;
        private int mProviderCount;

        public ValueContainerConfigurationProviderHandler(SQLiteDatabase database) {
            mDatabase = database;
        }

        public int getProviderCount() {
            return mProviderCount;
        }

        @Override
        public void startDocument() throws SAXException {
            mBuilder = new StringBuilder();
            mValues = new ContentValues();
            mProviderConfigId = getNextAutoIncrementId(TABLE_CONFIGURATION_PROVIDER);
            if (mProviderConfigId == 0) {
                throw new SqlExecuteFailed("provider id get failed");
            }
            mSensorConfigId = getNextAutoIncrementId(TABLE_SENSOR_CONFIGURATION);
            if (mSensorConfigId == 0) {
                throw new SqlExecuteFailed("sensor id get failed");
            }
            mMeasurementConfigId = getNextAutoIncrementId(TABLE_MEASUREMENT_CONFIGURATION);
            if (mMeasurementConfigId == 0) {
                throw new SqlExecuteFailed("measurement id get failed");
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (localName) {
                case "provider":
                    mProviderName = attributes.getValue(TAG_NAME);
                    break;
                case TAG_SENSOR:
                    mAddress = Integer.parseInt(attributes.getValue(TAG_ADDRESS), 16);
                    break;
                case TAG_MEASUREMENT:
                    mIntoMeasurementElement = true;
                    String index = attributes.getValue("index");
                    mMeasurementValueId = Sensor.Measurement.ID.getId(mAddress,
                            Byte.parseByte(attributes.getValue(TAG_TYPE), 16),
                            TextUtils.isEmpty(index) ? 0 : Integer.parseInt(index));
                    break;
                case TAG_WARNER:
                    mWarnerType = attributes.getValue(TAG_TYPE);
                    break;
            }
            mBuilder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
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
                        throw new LackParameterException("sensor address and measurement type may not be empty");
                    }
                    mValues.put(COLUMN_MEASUREMENT_VALUE_ID, mMeasurementValueId);
                    if (!TextUtils.isEmpty(mMeasurementName)) {
                        mValues.put(COLUMN_CUSTOM_NAME, mMeasurementName);
                    }
                    if (database.insert(TABLE_MEASUREMENT_CONFIGURATION, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert measurement configuration failed");
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
                        throw new LackParameterException("sensor address may not be empty");
                    }
                    mValues.put(COLUMN_SENSOR_ADDRESS, mAddress);
                    if (!TextUtils.isEmpty(mSensorName)) {
                        mValues.put(COLUMN_CUSTOM_NAME, mSensorName);
                    }
                    if (database.insert(TABLE_SENSOR_CONFIGURATION, null, mValues) == -1) {
                        throw new SqlExecuteFailed("insert sensor configuration failed");
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
