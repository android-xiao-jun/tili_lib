package com.example.record.analytics.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.record.analytics.core.PageTrackSession
import com.example.record.analytics.util.Logger

class PageTrackDao private constructor(context: Context) {

    private val dbHelper = PageTrackDbHelper.getInstance(context)

    fun insertSession(session: PageTrackSession): Long {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PageTrackContract.Columns.RECORD_ID, session.recordId)
                put(PageTrackContract.Columns.PAGE_NAME, session.pageName)
                put(PageTrackContract.Columns.ACTIVITY_NAME, session.activityName)
                put(PageTrackContract.Columns.START_TIME, session.startTime)
                put(PageTrackContract.Columns.END_TIME, 0L)
                put(PageTrackContract.Columns.DURATION_MS, 0L)
                put(PageTrackContract.Columns.LAST_UPDATE_TIME, session.startTime)
                put(PageTrackContract.Columns.UPLOAD_STATE, 0)
                put(PageTrackContract.Columns.EXTRA, session.extra)
            }
            db.insertWithOnConflict(
                PageTrackContract.TABLE_PAGE_STAY,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
            )
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "insertSession error", t)
            -1L
        }
    }

    /**
     * 同一个recordId只更新最后一次时长，不新增多条
     */
    fun updateDuration(recordId: String, durationMs: Long, endTime: Long, lastUpdateTime: Long): Int {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PageTrackContract.Columns.DURATION_MS, durationMs)
                put(PageTrackContract.Columns.END_TIME, endTime)
                put(PageTrackContract.Columns.LAST_UPDATE_TIME, lastUpdateTime)
                put(PageTrackContract.Columns.UPLOAD_STATE, 0) // 只要有更新就标记待上传
            }
            db.update(
                PageTrackContract.TABLE_PAGE_STAY,
                values,
                "${PageTrackContract.Columns.RECORD_ID}=?",
                arrayOf(recordId)
            )
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "updateDuration error", t)
            0
        }
    }

    fun updateUploadState(recordId: String, uploadState: Int): Int {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PageTrackContract.Columns.UPLOAD_STATE, uploadState)
            }
            db.update(
                PageTrackContract.TABLE_PAGE_STAY,
                values,
                "${PageTrackContract.Columns.RECORD_ID}=?",
                arrayOf(recordId)
            )
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "updateUploadState error", t)
            0
        }
    }

    fun queryPendingUpload(limit: Int = 50): List<PageStayRecord> {
        val list = mutableListOf<PageStayRecord>()
        var cursor: Cursor? = null
        try {
            val db = dbHelper.readableDatabase
            cursor = db.query(
                PageTrackContract.TABLE_PAGE_STAY,
                null,
                "${PageTrackContract.Columns.UPLOAD_STATE} IN (0,3)",
                null,
                null,
                null,
                "${PageTrackContract.Columns.START_TIME} ASC",
                limit.toString()
            )
            while (cursor.moveToNext()) {
                list.add(cursor.toRecord())
            }
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "queryPendingUpload error", t)
        } finally {
            cursor?.close()
        }
        return list
    }

    fun queryByRecordId(recordId: String): PageStayRecord? {
        var cursor: Cursor? = null
        return try {
            val db = dbHelper.readableDatabase
            cursor = db.query(
                PageTrackContract.TABLE_PAGE_STAY,
                null,
                "${PageTrackContract.Columns.RECORD_ID}=?",
                arrayOf(recordId),
                null,
                null,
                null,
                "1"
            )
            if (cursor.moveToFirst()) cursor.toRecord() else null
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "queryByRecordId error", t)
            null
        } finally {
            cursor?.close()
        }
    }

    fun deleteUploadedBefore(timeMs: Long, limit: Int = 200): Int {
        return try {
            val db = dbHelper.writableDatabase
            db.delete(
                PageTrackContract.TABLE_PAGE_STAY,
                "${PageTrackContract.Columns.UPLOAD_STATE}=1 AND ${PageTrackContract.Columns.END_TIME} > 0 AND ${PageTrackContract.Columns.END_TIME} < ?",
                arrayOf(timeMs.toString())
            )
        } catch (t: Throwable) {
            Logger.e("PageTrackDao", "deleteUploadedBefore error", t)
            0
        }
    }

    private fun Cursor.toRecord(): PageStayRecord {
        return PageStayRecord(
            id = getLong(getColumnIndexOrThrow(PageTrackContract.Columns.ID)),
            recordId = getString(getColumnIndexOrThrow(PageTrackContract.Columns.RECORD_ID)),
            pageName = getString(getColumnIndexOrThrow(PageTrackContract.Columns.PAGE_NAME)),
            activityName = getString(getColumnIndexOrThrow(PageTrackContract.Columns.ACTIVITY_NAME)),
            startTime = getLong(getColumnIndexOrThrow(PageTrackContract.Columns.START_TIME)),
            endTime = getLong(getColumnIndexOrThrow(PageTrackContract.Columns.END_TIME)),
            durationMs = getLong(getColumnIndexOrThrow(PageTrackContract.Columns.DURATION_MS)),
            lastUpdateTime = getLong(getColumnIndexOrThrow(PageTrackContract.Columns.LAST_UPDATE_TIME)),
            uploadState = getInt(getColumnIndexOrThrow(PageTrackContract.Columns.UPLOAD_STATE)),
            extra = getString(getColumnIndexOrThrow(PageTrackContract.Columns.EXTRA))
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: PageTrackDao? = null

        fun getInstance(context: Context): PageTrackDao {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PageTrackDao(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

data class PageStayRecord(
    val id: Long,
    val recordId: String,
    val pageName: String,
    val activityName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val lastUpdateTime: Long,
    val uploadState: Int,
    val extra: String?
)