package com.example.record.analytics.util

import android.util.Log
object Logger {

    @Volatile
    var enable: Boolean = false

    @Volatile
    private var externalLogger: PageTrackLogger? = null

    /**
     * 宿主App注入自定义日志实现
     */
    fun setLogger(logger: PageTrackLogger?) {
        externalLogger = logger
    }

    fun d(tag: String, msg: String) {
        if (!enable) return
        try {
            (externalLogger ?: DefaultPageTrackLogger).d(tag, msg)
        } catch (_: Throwable) {
            // 避免外部日志实现异常影响主流程
        }
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (!enable) return
        try {
            (externalLogger ?: DefaultPageTrackLogger).e(tag, msg, tr)
        } catch (_: Throwable) {
            // 避免外部日志实现异常影响主流程
        }
    }
}