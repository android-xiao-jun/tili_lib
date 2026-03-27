package com.example.record.analytics.util

import android.util.Log

internal object DefaultPageTrackLogger : PageTrackLogger {

    private const val GLOBAL_TAG = "PageTrack"

    override fun d(tag: String, msg: String) {
        Log.d("$GLOBAL_TAG-$tag", msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        Log.e("$GLOBAL_TAG-$tag", msg, tr)
    }
}