package com.weisi.tool.wsnbox.io.database

import android.database.sqlite.SQLiteDatabase

interface DatabaseUpdater {

    fun onVersionUpdateToBrokenTime(db: SQLiteDatabase, builder: StringBuilder)
}