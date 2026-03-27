package com.example.record.analytics.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PageTrackDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, PageTrackContract.DB_NAME, null, PageTrackContract.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PageTrackContract.SQL_CREATE_TABLE)
        db.execSQL(PageTrackContract.SQL_CREATE_INDEX_1)
        db.execSQL(PageTrackContract.SQL_CREATE_INDEX_2)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 当前 V1，无升级逻辑
        // 后续新增字段时，按版本写 ALTER TABLE
    }

    companion object {
        @Volatile
        private var INSTANCE: PageTrackDbHelper? = null

        fun getInstance(context: Context): PageTrackDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PageTrackDbHelper(context).also { INSTANCE = it }
            }
        }
    }
}