package com.example.record.analytics.api

import com.example.record.analytics.db.PageStayRecord

open class DefaultPageTrackUploader : PageTrackUploader {
    override fun upload(records: List<PageStayRecord>, callback: (Boolean) -> Unit) {
        // 默认空实现，避免宿主未注入时崩溃
        callback(false)
    }

    override fun heartbeat(record: PageStayRecord) {
        //默认空实现
    }
}