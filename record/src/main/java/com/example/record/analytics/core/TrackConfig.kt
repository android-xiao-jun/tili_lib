package com.example.record.analytics.core

data class TrackConfig(
    val enableLog: Boolean = false,
    val heartbeatIntervalMs: Long = 10_000L,
    val heartbeatIntervalUpload: Boolean = false,
    val ignoreActivities: Set<String> = emptySet(),
    val autoUploadOnPageLeave: Boolean = false
)