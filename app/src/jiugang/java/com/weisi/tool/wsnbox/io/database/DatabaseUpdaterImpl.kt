package com.weisi.tool.wsnbox.io.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.io.Constant
import java.util.*

class DatabaseUpdaterImpl : DatabaseUpdater {

    override fun onVersionUpdateToBrokenTime(db: SQLiteDatabase, builder: StringBuilder) {
        var cursor: Cursor? = null
        try {
            val contentValues = ContentValues()
            builder.setLength(0);
            builder.append("SELECT ").append(Constant.COLUMN_SENSOR_ADDRESS)
                    .append(',').append(Constant.COLUMN_TIMESTAMP)
                    .append(" FROM ").append(Constant.TABLE_SENSOR_INIT_DATA)
            cursor = db.rawQuery(builder.toString(), null)
            var addressIndex = cursor.getColumnIndex(Constant.COLUMN_SENSOR_ADDRESS)
            var timestampIndex = cursor.getColumnIndex(Constant.COLUMN_TIMESTAMP)
            var address: Int
            while (cursor.moveToNext()) {
                address = cursor.getInt(addressIndex)
                if (!Sensor.ID.isBleProtocolFamily(address)) {
                    contentValues.put(Constant.COLUMN_SENSOR_ADDRESS, address)
                    contentValues.put(Constant.COLUMN_TIMESTAMP, correctTimestamp(cursor.getLong(timestampIndex)))
                    db.update(Constant.TABLE_SENSOR_INIT_DATA, contentValues, null, null)
                }
            }
            cursor.close()

            builder.setLength(0);
            builder.append("SELECT ").append(Constant.COLUMN_SENSOR_ADDRESS)
                    .append(',').append(Constant.COLUMN_TIMESTAMP)
                    .append(" FROM ").append(Constant.TABLE_SENSOR_DATA)
            cursor = db.rawQuery(builder.toString(), null)
            addressIndex = cursor.getColumnIndex(Constant.COLUMN_SENSOR_ADDRESS)
            timestampIndex = cursor.getColumnIndex(Constant.COLUMN_TIMESTAMP)
            while (cursor.moveToNext()) {
                address = cursor.getInt(addressIndex)
                if (!Sensor.ID.isBleProtocolFamily(address)) {
                    contentValues.put(Constant.COLUMN_SENSOR_ADDRESS, address)
                    contentValues.put(Constant.COLUMN_TIMESTAMP, correctTimestamp(cursor.getLong(timestampIndex)))
                    db.update(Constant.TABLE_SENSOR_DATA, contentValues, null, null)
                }
            }
            cursor.close()

            contentValues.clear()
            builder.setLength(0)
            builder.append("SELECT ").append(Constant.COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(Constant.COLUMN_TIMESTAMP)
                    .append(" FROM ").append(Constant.TABLE_MEASUREMENT_INIT_DATA)
            cursor = db.rawQuery(builder.toString(), null)
            var idIndex = cursor.getColumnIndex(Constant.COLUMN_MEASUREMENT_VALUE_ID)
            timestampIndex = cursor.getColumnIndex(Constant.COLUMN_TIMESTAMP)
            var id: Long
            while (cursor.moveToNext()) {
                id = cursor.getLong(idIndex)
                if (!Sensor.ID.isBleProtocolFamily(id)) {
                    contentValues.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, id)
                    contentValues.put(Constant.COLUMN_TIMESTAMP, correctTimestamp(cursor.getLong(timestampIndex)))
                    db.update(Constant.TABLE_MEASUREMENT_INIT_DATA, contentValues, null, null)
                }
            }
            cursor.close()

            contentValues.clear()
            builder.setLength(0)
            builder.append("SELECT ").append(Constant.COLUMN_MEASUREMENT_VALUE_ID)
                    .append(',').append(Constant.COLUMN_TIMESTAMP)
                    .append(" FROM ").append(Constant.TABLE_MEASUREMENT_DATA)
            cursor = db.rawQuery(builder.toString(), null)
            idIndex = cursor.getColumnIndex(Constant.COLUMN_MEASUREMENT_VALUE_ID)
            timestampIndex = cursor.getColumnIndex(Constant.COLUMN_TIMESTAMP)
            while (cursor.moveToNext()) {
                id = cursor.getLong(idIndex)
                if (!Sensor.ID.isBleProtocolFamily(id)) {
                    contentValues.put(Constant.COLUMN_MEASUREMENT_VALUE_ID, id)
                    contentValues.put(Constant.COLUMN_TIMESTAMP, correctTimestamp(cursor.getLong(timestampIndex)))
                    db.update(Constant.TABLE_MEASUREMENT_DATA, contentValues, null, null)
                }
            }
            cursor.close()
        } finally {
            cursor?.close()
        }
    }

    private fun correctTimestamp(errorTime: Long) : Long {
        val c = Calendar.getInstance()
        c.timeInMillis = errorTime
        c.add(Calendar.MONTH, -1)
        return c.timeInMillis
    }
}