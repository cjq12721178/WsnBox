package com.weisi.tool.wsnbox.io;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.data.SensorData;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/11/9.
 */

public class SensorDatabase {

    private static final String DATABASE_NAME = "BaseSensorStorage.db";
    private static final int VN_WINDOW = 1;
    private static final int CURRENT_VERSION_NO = VN_WINDOW;

    private static final String TABLE_SENSOR_INIT_DATA = "sensor_init_data";
    private static final String TABLE_MEASUREMENT_INIT_DATA = "measurement_init_data";
    private static final String TABLE_SENSOR_DATA = "sensor_data";
    private static final String TABLE_MEASUREMENT_DATA = "measurement_data";
    private static final String COLUMN_SENSOR_ADDRESS = "address";
    private static final String COLUMN_TIMESTAMP = "time";
    private static final String COLUMN_BATTER_VOLTAGE = "voltage";
    //private static final String COLUMN_DATA_TYPE_VALUE = "data_type";
    //private static final String COLUMN_DATA_TYPE_VALUE_INDEX = "type_index";
    private static final String COLUMN_RAW_VALUE = "value";
    private static final String COLUMN_MEASUREMENT_VALUE_ID = "value_id";

    private static SQLiteLauncher launcher;
    private static SQLiteDatabase database;
//    private static String lastErrorMessage = "";
//    private static int sameErrorContinuousOccurTimes = 0;
//    private static List<String> errorBlackList = new LinkedList<>();

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
        } catch (Exception e) {
            ExceptionLog.process(e);
        }
        return database != null;
    }

    public static void shutdown() {
        if (launcher != null) {
            launcher.close();
            database = null;
            launcher = null;
        }
    }

//    public static int getSensorHistoryValueSize(int address) {
//        if (database == null) {
//            return -1;
//        }
//        Cursor cursor = null;
//        try {
//            StringBuffer buffer = new StringBuffer();
//            String countColumnName = "value_size";
//            cursor = database.rawQuery(buffer
//                    .append("SELECT COUNT(DISTINCT ").append(COLUMN_TIMESTAMP)
//                    .append(") AS ").append(countColumnName)
//                    .append(" FROM ").append(TABLE_SENSOR_HISTORY)
//                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(" = ").append(address)
//                    .toString(), null);
//            if (cursor == null || !cursor.moveToNext()) {
//                return -1;
//            }
//            return cursor.getInt(cursor.getColumnIndex(countColumnName));
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return -1;
//    }

//    public static boolean importSensorHistoryValues(int address,
//                                                    long startTime,
//                                                    long endTime,
//                                                    int limitCount,
//                                                    SensorHistoryInfoReceiver receiver) {
//        if (database == null || receiver == null) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "start import sensor history values : " + dateFormat.format(new Date()));
//            StringBuffer buffer = new StringBuffer();
//            buffer.append("SELECT * FROM ").append(TABLE_SENSOR_HISTORY)
//                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(" = ").append(address);
//            if (startTime <= 0) {
//                buffer.append(" AND ");
//                if (endTime <= 0) {
//                    buffer.append(COLUMN_TIMESTAMP)
//                            .append(" > ")
//                            .append(startTime);
//                } else {
//                    buffer.append('(').append(COLUMN_TIMESTAMP)
//                            .append(" BETWEEN ").append(startTime)
//                            .append(" AND ").append(endTime)
//                            .append(')');
//                }
//            }
//            buffer.append(" ORDER BY ").append(COLUMN_TIMESTAMP);
//            if (limitCount > 0) {
//                buffer.append(" LIMIT ").append(limitCount);
//            }
//            cursor = database.rawQuery(buffer.toString(), null);
//            ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "finish select sentence : " + dateFormat.format(new Date()));
//            return receiveSensorData(cursor, receiver, address);
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

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

//    public static boolean importSensorEarliestValue(SensorHistoryInfoReceiver receiver) {
//        if (database == null || receiver == null) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "start import sensor earliest value : " + dateFormat.format(new Date()));
//            //CodeRunTimeCatcher.start();
//            StringBuffer buffer = new StringBuffer();
//            cursor = database.rawQuery(buffer
//                    .append("SELECT a.* FROM ").append(TABLE_SENSOR_HISTORY)
//                    .append(" AS a WHERE ").append(COLUMN_TIMESTAMP)
//                    .append(" = (SELECT MIN(").append(COLUMN_TIMESTAMP)
//                    .append(") FROM ").append(TABLE_SENSOR_HISTORY)
//                    .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                    .append(" = a.").append(COLUMN_SENSOR_ADDRESS)
//                    .append(')').toString(), null);
//            //ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "import sensor earliest value spend time = " + CodeRunTimeCatcher.end());
//            ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "finish select sentence : " + dateFormat.format(new Date()));
//            return receiveSensorData(cursor, receiver, -1);
//        } catch (Exception e) {
//            ExceptionLog.record(e);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

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
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

//    private static boolean receiveSensorData(Cursor cursor, SensorHistoryInfoReceiver receiver, int address) {
//        if (cursor == null) {
//            return false;
//        }
//        ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "start receive sensor data : " + dateFormat.format(new Date()));
//        int addressIndex = cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS);
//        int dataTypeValueIndex = cursor.getColumnIndex(COLUMN_DATA_TYPE_VALUE);
//        int dataTypeValueIndexIndex = cursor.getColumnIndex(COLUMN_DATA_TYPE_VALUE_INDEX);
//        int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);
//        int voltageIndex = cursor.getColumnIndex(COLUMN_BATTER_VOLTAGE);
//        int rawValueIndex = cursor.getColumnIndex(COLUMN_RAW_VALUE);
//        while (cursor.moveToNext()) {
//            //CodeRunTimeCatcher.start();
//            ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "receiving : " + dateFormat.format(new Date()));
//            receiver.onSensorDataReceived(
//                    cursor.getLong(timestampIndex),
//                    cursor.getFloat(voltageIndex)
//            );
//            //ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "on sensor data received spend time = " + CodeRunTimeCatcher.end());
//        }
//        ClosableLog.d(Tag.LOG_TAG_D_CODE_RUN_TIME, "finish receive sensor data : " + dateFormat.format(new Date()));
//        return true;
//    }

    public interface SensorHistoryInfoReceiver {
//        void onSensorDataReceived(int address,
//                                  byte dataTypeValue,
//                                  int dataTypeValueIndex,
//                                  long timestamp,
//                                  float batteryVoltage,
//                                  double rawValue);
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
//            String currentErrorMessage = e.getMessage();
//            if (lastErrorMessage.equals(currentErrorMessage)) {
//                ++sameErrorContinuousOccurTimes;
//            } else {
//                lastErrorMessage = new String(currentErrorMessage);
//                sameErrorContinuousOccurTimes = 0;
//            }
//            if (sameErrorContinuousOccurTimes <= MAX_RECORDABLE_CONTINUOUS_ERROR_TIMES) {
//                if (!errorBlackList.contains(currentErrorMessage)) {
//                    ExceptionLog.record(e);
//                }
//            } else {
//                errorBlackList.add(currentErrorMessage);
//            }
        } finally {
            database.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

//    public static boolean batchSaveSensorData(SensorDataProvider provider) {
//        if (database == null || provider == null) {
//            return false;
//        }
//        Cursor cursor = null;
//        try {
//            StringBuffer buffer = new StringBuffer();
//            database.beginTransaction();
//            buffer.append("INSERT OR IGNORE INTO ");
//            int insertPrefixLen = buffer.length();
//            buffer.append(TABLE_SENSOR_DATA)
//                    .append(" (").append(COLUMN_SENSOR_ADDRESS)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_BATTER_VOLTAGE)
//                    .append(") VALUES (");
//            String normalInsertSentence = buffer.toString();
//            int normalInsertWholePrefixLength = buffer.length();
//            boolean insertSentenceChanged = false;
//            SensorData sensorData;
//            for (int i = 0,
//                 count = provider.getSensorDataCount();
//                 (count <= 0 || i < count)
//                         && (sensorData = provider.provideSensorData()) != null;
//                 ++i) {
//                switch (provider.getSensorDataState(sensorData)) {
//                    case SensorDataProvider.DUPLICATE_DATA:
//                        continue;
//                    case SensorDataProvider.NORMAL_DATA:
//                        if (insertSentenceChanged) {
//                            buffer.replace(insertPrefixLen, insertPrefixLen + TABLE_SENSOR_INIT_DATA.length(), TABLE_SENSOR_DATA);
//                            insertSentenceChanged = false;
//                        }
//                        buffer.setLength(normalInsertWholePrefixLength);
//                        break;
//                    case SensorDataProvider.FIRST_DATA:
//                        buffer.setLength(0);
//                        cursor = database.rawQuery(buffer
//                                .append("SELECT ").append(COLUMN_SENSOR_ADDRESS)
//                                .append(" FROM ").append(TABLE_SENSOR_INIT_DATA)
//                                .append(" WHERE ").append(COLUMN_SENSOR_ADDRESS)
//                                .append(" = ").append(sensorData.getAddress())
//                                .toString(), null);
//                        buffer.setLength(0);
//                        buffer.append(normalInsertSentence);
//                        if (cursor == null
//                                || !cursor.moveToNext()
//                                || cursor.getInt(cursor.getColumnIndex(COLUMN_SENSOR_ADDRESS)) != sensorData.getAddress()) {
//                            buffer.replace(insertPrefixLen, insertPrefixLen + TABLE_SENSOR_DATA.length(), TABLE_SENSOR_INIT_DATA);
//                            insertSentenceChanged = true;
//                        }
//                        break;
//                }
//                database.execSQL(buffer.append(sensorData.getAddress())
//                        .append(',').append(sensorData.getTimestamp())
//                        .append(',').append(sensorData.getBatteryVoltage())
//                        .append(')').toString());
//                sensorData.recycle();
//            }
//            buffer.setLength(insertPrefixLen);
//            buffer.append(TABLE_MEASUREMENT_DATA)
//                    .append(" (").append(COLUMN_MEASUREMENT_VALUE_ID)
//                    .append(',').append(COLUMN_TIMESTAMP)
//                    .append(',').append(COLUMN_RAW_VALUE)
//                    .append(") VALUES (");
//            normalInsertSentence = buffer.toString();
//            normalInsertWholePrefixLength = buffer.length();
//            insertSentenceChanged = false;
//            MeasurementData measurementData;
//            for (int i = 0,
//                 count = provider.getMeasurementDataCount();
//                 (count <= 0 || i < count)
//                         && (measurementData = provider.provideMeasurementData()) != null;
//                 ++i) {
//                switch (provider.getMeasurementDataState(measurementData)) {
//                    case SensorDataProvider.DUPLICATE_DATA:
//                        continue;
//                    case SensorDataProvider.NORMAL_DATA:
//                        if (insertSentenceChanged) {
//                            buffer.replace(insertPrefixLen, insertPrefixLen + TABLE_MEASUREMENT_INIT_DATA.length(), TABLE_MEASUREMENT_DATA);
//                            insertSentenceChanged = false;
//                        }
//                        buffer.setLength(normalInsertWholePrefixLength);
//                        break;
//                    case SensorDataProvider.FIRST_DATA:
//                        buffer.setLength(0);
//                        cursor = database.rawQuery(buffer
//                                .append("SELECT ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                                .append(" FROM ").append(TABLE_MEASUREMENT_INIT_DATA)
//                                .append(" WHERE ").append(COLUMN_MEASUREMENT_VALUE_ID)
//                                .append(" = ").append(measurementData.getId())
//                                .toString(), null);
//                        buffer.setLength(0);
//                        buffer.append(normalInsertSentence);
//                        if (cursor != null
//                                && cursor.moveToNext()
//                                && cursor.getLong(cursor.getColumnIndex(COLUMN_MEASUREMENT_VALUE_ID)) != measurementData.getId()) {
//                            buffer.replace(insertPrefixLen, insertPrefixLen + TABLE_MEASUREMENT_DATA.length(), TABLE_MEASUREMENT_INIT_DATA);
//                            insertSentenceChanged = true;
//                        }
//                        break;
//                }
//                database.execSQL(buffer.append(measurementData.getId())
//                        .append(',').append(measurementData.getTimestamp())
//                        .append(',').append(measurementData.getRawValue())
//                        .append(')').toString());
//                measurementData.recycle();
//            }
//            database.setTransactionSuccessful();
//            return true;
//        } catch (Exception e) {
//            ExceptionLog.debug(e);
//        } finally {
//            database.endTransaction();
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

    public interface SensorDataProvider {
        //通过getSensorDataState或者getMeasurementDataState返回
        //传感器或者测量量首条需要保存的历史数据，
        //但并不意味着其在数据库中也是首条，
        //所以需查询TABLE_SENSOR_INIT_DATA或者TABLE_MEASUREMENT_INIT_DATA
        //予以确认
        public static final int FIRST_DATA = 1;
        //普通数据，存放于TABLE_SENSOR_DATA和TABLE_MEASUREMENT_DATA
        public static final int NORMAL_DATA = 2;
        //根据配置规则确定的重复数据，无需保存
        public static final int DUPLICATE_DATA = 3;
        int getSensorDataCount();
        SensorData provideSensorData();
        int getSensorDataState(SensorData data);
        //int getMeasurementDataCount();
        //MeasurementData provideMeasurementData();
        //int getMeasurementDataState(MeasurementData data);
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
                    //.append(COLUMN_SENSOR_ADDRESS).append(" INT NOT NULL,")
                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
                    //.append(COLUMN_DATA_TYPE_VALUE).append(" TINYINT NOT NULL,")
                    //.append(COLUMN_DATA_TYPE_VALUE_INDEX).append(" INT NOT NULL,")
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

//        private void createSensorHistoryDataTable(SQLiteDatabase db, StringBuilder builder) {
//            builder.setLength(0);
//            db.execSQL(builder
//                    .append("CREATE TABLE ").append(TABLE_SENSOR_HISTORY)
//                    .append(" (").append("id INT AUTO_INCREMENT PRIMARY KEY,")
//                    .append(COLUMN_SENSOR_ADDRESS).append(" INT NOT NULL,")
//                    .append(COLUMN_DATA_TYPE_VALUE).append(" TINYINT NOT NULL,")
//                    .append(COLUMN_DATA_TYPE_VALUE_INDEX).append(" INT NOT NULL,")
//                    .append(COLUMN_TIMESTAMP).append(" BIGINT NOT NULL,")
//                    .append(COLUMN_BATTER_VOLTAGE).append(" FLOAT NOT NULL,")
//                    .append(COLUMN_RAW_VALUE).append(" DOUBLE NOT NULL)")
//                    .toString());
//        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
