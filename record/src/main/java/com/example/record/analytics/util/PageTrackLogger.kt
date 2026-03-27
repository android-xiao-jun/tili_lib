package com.example.record.analytics.util

interface PageTrackLogger {

    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, tr: Throwable? = null)
}