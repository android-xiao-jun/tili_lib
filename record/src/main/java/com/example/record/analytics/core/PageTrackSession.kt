package com.example.record.analytics.core

data class PageTrackSession(
    val recordId: String,
    val pageName: String,
    val activityName: String,
    val startTime: Long,
    val extra: String? = null
)