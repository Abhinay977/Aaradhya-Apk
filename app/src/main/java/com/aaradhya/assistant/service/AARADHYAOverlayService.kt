package com.aaradhya.assistant.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.aaradhya.assistant.R
import com.aaradhya.assistant.ui.main.MainActivity
import com.aaradhya.assistant.ui.main.OrbAnimationView

/**
 * Foreground service showing a draggable floating orb overlay.
 * Triggered by double-press power button.
 */
class AARADHYAOverlayService : Service() {

    companion object {
        private const val TAG        = "OverlayService"
        const val CHANNEL_ID         = "aaradhya_overlay_channel"
        private const val NOTIF_ID   = 1
        const val ACTION_SHOW        = "SHOW_OVERLAY"

        @Volatile var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var overlayView:   View? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            startForeground(NOTIF_ID, buildNotification(), type)
        } else {
            startForeground(NOTIF_ID, buildNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SHOW) {
            showOverlay()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        removeOverlay()
    }

    // ── Overlay ────────────────────────────────────────────────────────────────

    private fun showOverlay() {
        if (overlayView != null) return  // Already showing

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_orb, null)

        val params = WindowManager.LayoutParams(
            dpToPx(160),
            dpToPx(160),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Drag support
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f

        overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager?.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Short tap → open MainActivity
                    if (Math.abs(event.rawX - touchX) < 10 && Math.abs(event.rawY - touchY) < 10) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }

        // Close button
        overlayView?.findViewById<ImageButton>(R.id.overlayCloseBtn)?.setOnClickListener {
            removeOverlay()
        }

        // Start orb animation
        overlayView?.findViewById<OrbAnimationView>(R.id.overlayOrb)?.setState(OrbAnimationView.State.IDLE)

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "showOverlay error: ${e.message}")
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.e(TAG, "removeOverlay error: ${e.message}")
        }
        overlayView = null
    }

    // ── Notification ───────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AARADHYA Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "AARADHYA floating orb overlay" }
        val nm = getSystemService(NotificationManager::class.java)
        nm?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AARADHYA")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_aaradhya_notif)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
