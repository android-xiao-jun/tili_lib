package com.example.record.analytics

import android.app.Application
import android.content.Context
import com.example.record.analytics.api.PageTrackBridge
import com.example.record.analytics.api.PageTrackUploader
import com.example.record.analytics.core.PageTrackLifecycleCallbacks
import com.example.record.analytics.core.PageTrackManager
import com.example.record.analytics.core.TrackConfig
import com.example.record.analytics.util.Logger
import com.example.record.analytics.util.PageTrackLogger
import java.util.concurrent.atomic.AtomicBoolean

object PageTracker {

    private val initialized = AtomicBoolean(false)
    @Volatile
    private var application: Application? = null

    @Volatile
    private var lifecycleCallbacks: PageTrackLifecycleCallbacks? = null

    @Volatile
    private var config: TrackConfig = TrackConfig()

    /**
     * Provider自动初始化时调用
     */
    internal fun autoInit(context: Context) {
        val app = context.applicationContext as? Application ?: return
        if (!isMainProcess(app)) return
        init(app)
    }

    private fun isMainProcess(context: Context): Boolean {
        val packageName = context.packageName
        val processName = android.os.Process.myPid().let { pid ->
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
        }
        return packageName == processName
    }

    @Synchronized
    fun init(app: Application, config: TrackConfig = PageTracker.config) {
        if (initialized.get()) {
            // 支持重复调用时只更新配置
            PageTrackManager.getInstance(app).updateConfig(config)
            PageTracker.config = config
            return
        }

        application = app
        PageTracker.config = config

        val manager = PageTrackManager.getInstance(app)
        manager.updateConfig(config)

        val callbacks = PageTrackLifecycleCallbacks(manager)
        app.registerActivityLifecycleCallbacks(callbacks)
        lifecycleCallbacks = callbacks

        initialized.set(true)
    }

    fun updateConfig(config: TrackConfig) {
        PageTracker.config = config
        application?.let {
            PageTrackManager.getInstance(it).updateConfig(config)
        }
    }

    fun setUploader(uploader: PageTrackUploader?) {
        PageTrackBridge.setUploader(uploader)
    }

    fun setLogger(logger: PageTrackLogger?) {
        Logger.setLogger(logger)
    }

    /**
     * 手动触发上传（例如app切后台、登录成功、网络恢复）
     */
    fun triggerUpload() {
        application?.let {
            PageTrackManager.getInstance(it).triggerUpload()
        }
    }
}