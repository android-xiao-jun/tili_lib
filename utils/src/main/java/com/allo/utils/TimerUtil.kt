package com.allo.utils

import android.os.Handler
import android.os.Looper

class TimerUtil(private val intervalInMillis: Long) {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var isTimerRunning: Boolean = false
    private var elapsedMillis: Long = 0L
    private var onTickListener: OnTickListener? = null

    interface OnTickListener {
        fun onTick(timeInMillis: Long)
    }

    fun setOnTickListener(listener: OnTickListener) {
        onTickListener = listener
    }

    fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            elapsedMillis = 0L
            startTimerRunnable()
        }
    }

    fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun startTimerRunnable() {
        handler.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    elapsedMillis += intervalInMillis

                    onTickListener?.onTick(elapsedMillis)

                    handler.postDelayed(this, intervalInMillis)
                }
            }
        })
    }
}
