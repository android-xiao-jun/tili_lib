package com.example.record.analytics.api


object PageTrackBridge {

    @Volatile
    internal var default: PageTrackUploader = DefaultPageTrackUploader()

    internal var uploader: PageTrackUploader = default

    fun setUploader(uploader: PageTrackUploader?) {
        this.uploader = uploader ?: default
    }
}