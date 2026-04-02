package com.example.record.analytics.core

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.example.record.analytics.api.PageTrackBridge
import com.example.record.analytics.db.PageTrackDao
import com.example.record.analytics.util.IdGenerator
import com.example.record.analytics.util.Logger
import java.util.WeakHashMap

internal class PageTrackManager private constructor(
    private val context: Context
) {

    private val dao = PageTrackDao.getInstance(context)
    private val dispatcher = PageTrackDispatcher.getInstance(context)

    private val workerThread = HandlerThread("page-track-worker").apply { start() }
    private val workerHandler = Handler(workerThread.looper)

    @Volatile
    private var config: TrackConfig = TrackConfig()

    /**
     * 每个Activity一个独立session，防止A->B覆盖
     */
    private val sessionMap = WeakHashMap<Activity, ActiveSession>()

    fun updateConfig(config: TrackConfig) {
        this.config = config
        Logger.enable = config.enableLog
    }

    fun onActivityResumed(activity: Activity) {
        if (shouldIgnore(activity)) return

        val now = System.currentTimeMillis()
        val recordId = IdGenerator.newRecordId()
        val pageName = resolvePageName(activity)
        val activityName = activity.javaClass.name
        val extra = resolveExtra(activity)

        val session = ActiveSession(
            recordId = recordId,
            pageName = pageName,
            activityName = activityName,
            startWallTime = now,
            startElapsedTime = SystemClock.elapsedRealtime()
        )

        synchronized(sessionMap) {
            // 极端情况下如果已有旧session，先结束旧的，避免脏数据
            sessionMap.remove(activity)?.let { old ->
                finishSessionInternal(old, finalUpload = false)
            }
            sessionMap[activity] = session
        }

        Logger.d("Manager", "resume page=$pageName recordId=$recordId")

        workerHandler.post {
            dao.insertSession(
                PageTrackSession(
                    recordId = recordId,
                    pageName = pageName,
                    activityName = activityName,
                    startTime = now,
                    extra = extra
                )
            )
        }

        scheduleHeartbeat(session)
    }

    fun onActivityPaused(activity: Activity) {
        if (shouldIgnore(activity)) return

        val session = synchronized(sessionMap) {
            sessionMap.remove(activity)
        } ?: return

        Logger.d("Manager", "pause page=${session.pageName} recordId=${session.recordId}")

        stopHeartbeat(session)
        finishSessionInternal(session, finalUpload = config.autoUploadOnPageLeave)
    }

    private fun finishSessionInternal(session: ActiveSession, finalUpload: Boolean) {
        val now = System.currentTimeMillis()
        val duration = calcDuration(session)

        workerHandler.post {
            dao.updateDuration(
                recordId = session.recordId,
                durationMs = duration,
                endTime = now,
                lastUpdateTime = now
            )
            Logger.d("Manager", "final update recordId=${session.recordId}, duration=$duration")

            if (finalUpload) {
                dispatcher.tryUploadAsync()
            }
        }
    }

    private fun scheduleHeartbeat(session: ActiveSession) {
        val task = object : Runnable {
            override fun run() {
                if (!session.active) return

                val now = System.currentTimeMillis()
                val duration = calcDuration(session)

                workerHandler.post {
                    dao.updateDuration(
                        recordId = session.recordId,
                        durationMs = duration,
                        endTime = 0L, // 页面未结束，保持0更清晰
                        lastUpdateTime = now
                    )
                    Logger.d("Manager", "heartbeat recordId=${session.recordId}, duration=$duration")
                }

                session.heartbeatRunnable = this
                mainHandler.postDelayed(this, config.heartbeatIntervalMs)

                workerHandler.post {
                    if (config.heartbeatIntervalUpload){
                        val pending = dao.queryByRecordId(session.recordId)
                        if (pending != null) {
                            Logger.d("Manager", "heartbeat uploader recordId=${session.recordId}")
                            PageTrackBridge.uploader.heartbeat(pending)
                        }
                    }
                }
            }
        }

        session.heartbeatRunnable = task
        mainHandler.postDelayed(task, config.heartbeatIntervalMs)
    }

    private fun stopHeartbeat(session: ActiveSession) {
        session.active = false
        session.heartbeatRunnable?.let {
            mainHandler.removeCallbacks(it)
        }
        session.heartbeatRunnable = null
    }

    private fun calcDuration(session: ActiveSession): Long {
        return (SystemClock.elapsedRealtime() - session.startElapsedTime).coerceAtLeast(0L)
    }

    private fun resolvePageName(activity: Activity): String {
        return if (activity is PageTrackNameProvider) {
            activity.pageTrackName()
        } else {
            activity.javaClass.simpleName
        }
    }

    private fun resolveExtra(activity: Activity): String? {
        return if (activity is PageTrackExtraProvider) {
            activity.pageTrackExtra()
        } else {
            null
        }
    }

    private fun shouldIgnore(activity: Activity): Boolean {
        val name = activity.javaClass.name
        return config.ignoreActivities.contains(name)
    }

    fun triggerUpload() {
        dispatcher.tryUploadAsync()
    }

    fun release() {
        synchronized(sessionMap) {
            sessionMap.values.forEach {
                stopHeartbeat(it)
                finishSessionInternal(it, finalUpload = false)
            }
            sessionMap.clear()
        }
        workerThread.quitSafely()
        dispatcher.release()
    }

    companion object {
        private val mainHandler by lazy { Handler(android.os.Looper.getMainLooper()) }

        @Volatile
        private var INSTANCE: PageTrackManager? = null

        fun getInstance(context: Context): PageTrackManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PageTrackManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private data class ActiveSession(
        val recordId: String,
        val pageName: String,
        val activityName: String,
        val startWallTime: Long,
        val startElapsedTime: Long,
        @Volatile var active: Boolean = true,
        @Volatile var heartbeatRunnable: Runnable? = null
    )
}