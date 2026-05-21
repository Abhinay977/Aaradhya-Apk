package com.aaradhya.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Starts the AARADHYAOverlayService when the device boots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (android.provider.Settings.canDrawOverlays(context)) {
                try {
                    context.startForegroundService(
                        Intent(context, AARADHYAOverlayService::class.java)
                    )
                    android.util.Log.d("BootReceiver", "Started AARADHYAOverlayService at boot (overlay permission granted)")
                } catch (e: Exception) {
                    android.util.Log.e("BootReceiver", "Failed to start AARADHYAOverlayService at boot", e)
                }
            } else {
                android.util.Log.w("BootReceiver", "Skipping background start at boot: SYSTEM_ALERT_WINDOW not granted")
            }
        }
    }
}
