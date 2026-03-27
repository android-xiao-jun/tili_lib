package com.example.record.analytics.db

object PageTrackContract {

    const val DB_NAME = "page_track.db"
    const val DB_VERSION = 1

    const val TABLE_PAGE_STAY = "page_stay_record"

    object Columns {
        const val ID = "_id"
        const val RECORD_ID = "record_id"
        const val PAGE_NAME = "page_name"
        const val ACTIVITY_NAME = "activity_name"
        const val START_TIME = "start_time"
        const val END_TIME = "end_time"
        const val DURATION_MS = "duration_ms"
        const val LAST_UPDATE_TIME = "last_update_time"
        const val UPLOAD_STATE = "upload_state" // 0未上传 1已上传 2上传中 3失败
        const val EXTRA = "extra"
    }

    const val SQL_CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_PAGE_STAY (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.RECORD_ID} TEXT NOT NULL UNIQUE,
            ${Columns.PAGE_NAME} TEXT NOT NULL,
            ${Columns.ACTIVITY_NAME} TEXT NOT NULL,
            ${Columns.START_TIME} INTEGER NOT NULL,
            ${Columns.END_TIME} INTEGER NOT NULL DEFAULT 0,
            ${Columns.DURATION_MS} INTEGER NOT NULL DEFAULT 0,
            ${Columns.LAST_UPDATE_TIME} INTEGER NOT NULL DEFAULT 0,
            ${Columns.UPLOAD_STATE} INTEGER NOT NULL DEFAULT 0,
            ${Columns.EXTRA} TEXT
        );
    """

    const val SQL_CREATE_INDEX_1 =
        "CREATE INDEX IF NOT EXISTS idx_page_stay_start_time ON $TABLE_PAGE_STAY(${Columns.START_TIME});"

    const val SQL_CREATE_INDEX_2 =
        "CREATE INDEX IF NOT EXISTS idx_page_stay_upload_state ON $TABLE_PAGE_STAY(${Columns.UPLOAD_STATE});"
}