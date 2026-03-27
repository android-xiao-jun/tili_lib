package com.example.record.analytics.api

import com.example.record.analytics.db.PageStayRecord

interface PageTrackUploader {

    /**
     * 由宿主App实现具体上传逻辑
     * 成功后回调 true，失败 false
     */
    fun upload(records: List<PageStayRecord>, callback: (Boolean) -> Unit)

    /**
     * 心跳回调  heartbeatIntervalMs 和  heartbeatIntervalUpload 开关控制
     */
    fun heartbeat(record: PageStayRecord)
}