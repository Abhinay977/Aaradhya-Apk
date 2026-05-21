package com.aaradhya.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Detects double power button press (SCREEN_OFF + SCREEN_ON within 600ms)
 * and triggers the AARADHYA overlay service.
 */
class PowerButtonReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PowerButtonReceiver"
        private const val DOUBLE_PRESS_TIMEOUT = 600L
        private var lastScreenOffTime = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                lastScreenOffTime = System.currentTimeMillis()
                Log.d(TAG, "Screen OFF detected")
            }
            Intent.ACTION_SCREEN_ON -> {
                val now = System.currentTimeMillis()
                val elapsed = now - lastScreenOffTime
                Log.d(TAG, "Screen ON detected, elapsed=${elapsed}ms")
                if (elapsed < DOUBLE_PRESS_TIMEOUT && lastScreenOffTime > 0) {
                    Log.d(TAG, "Double power press! Showing overlay.")
                    val serviceIntent = Intent(context, AARADHYAOverlayService::class.java).apply {
                        action = AARADHYAOverlayService.ACTION_SHOW
                    }
                    try {
                        context.startForegroundService(serviceIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start AARADHYAOverlayService: ${e.message}", e)
                    }
                }
                lastScreenOffTime = 0L
            }
        }
    }
}
