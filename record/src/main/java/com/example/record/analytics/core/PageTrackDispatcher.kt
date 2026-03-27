package com.example.record.analytics.core

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.example.record.analytics.api.PageTrackBridge
import com.example.record.analytics.db.PageTrackDao
import com.example.record.analytics.util.Logger

internal class PageTrackDispatcher private constructor(
    private val context: Context
) {

    private val dao = PageTrackDao.getInstance(context)
    private val thread = HandlerThread("page-track-dispatcher").apply { start() }
    private val handler = Handler(thread.looper)

    fun tryUploadAsync(limit: Int = 20) {
        handler.post {
            try {
                val pending = dao.queryPendingUpload(limit)
                if (pending.isEmpty()) {
                    Logger.d("Dispatcher", "no pending records")
                    return@post
                }

                // 先标记上传中
                pending.forEach {
                    dao.updateUploadState(it.recordId, 2)
                }

                PageTrackBridge.uploader.upload(pending) { success ->
                    handler.post {
                        pending.forEach {
                            dao.updateUploadState(it.recordId, if (success) 1 else 3)
                        }
                    }
                }
            } catch (t: Throwable) {
                Logger.e("Dispatcher", "tryUploadAsync error", t)
            }
        }
    }

    fun release() {
        thread.quitSafely()
    }

    companion object {
        @Volatile
        private var INSTANCE: PageTrackDispatcher? = null

        fun getInstance(context: Context): PageTrackDispatcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PageTrackDispatcher(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}