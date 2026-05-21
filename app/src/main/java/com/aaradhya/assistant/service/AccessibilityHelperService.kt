package com.aaradhya.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Full-power Accessibility Service for AARADHYA.
 *
 * Acts as a fallback + enhancement layer on top of normal Android Intents.
 * Use directly via [instance] from MainViewModel, or via Broadcast from anywhere.
 *
 * Capabilities:
 * - Global navigation: Back, Home, Recents
 * - Screenshot & Screen Lock (Android 9+)
 * - Click any visible UI text on screen
 * - Type into text fields
 * - Scroll up / down
 * - Swipe gestures (Android 7+)
 * - Detect currently active app package
 * - Open app via launcher (HOME → find icon → click) as fallback
 */
class AccessibilityHelperService : AccessibilityService() {

    companion object {
        private const val TAG = "AccessibilityHelper"

        // ── Broadcast Actions ──────────────────────────────────────────────────
        const val ACTION_SCREENSHOT  = "com.aaradhya.assistant.TAKE_SCREENSHOT"
        const val ACTION_LOCK        = "com.aaradhya.assistant.LOCK_SCREEN"
        const val ACTION_GO_HOME     = "com.aaradhya.assistant.GO_HOME"
        const val ACTION_GO_BACK     = "com.aaradhya.assistant.GO_BACK"
        const val ACTION_GO_RECENTS  = "com.aaradhya.assistant.GO_RECENTS"
        const val ACTION_SCROLL_UP   = "com.aaradhya.assistant.SCROLL_UP"
        const val ACTION_SCROLL_DOWN = "com.aaradhya.assistant.SCROLL_DOWN"
        const val ACTION_CLICK_TEXT  = "com.aaradhya.assistant.CLICK_TEXT"
        const val ACTION_TYPE_TEXT   = "com.aaradhya.assistant.TYPE_TEXT"
        const val EXTRA_TEXT         = "text"

        /** Singleton — set in onServiceConnected, cleared in onDestroy */
        @Volatile
        var instance: AccessibilityHelperService? = null
            private set

        /** Current foreground app package — updated on every window change event */
        @Volatile
        var currentPackage: String = ""
            private set

        /**
         * Returns true if the Accessibility Service is enabled in system settings.
         * Call this before trying to use the service.
         */
        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabled.contains(context.packageName, ignoreCase = true)
        }

        /**
         * Opens the Accessibility Settings page so the user can enable the service.
         */
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    // ── Broadcast Receiver ─────────────────────────────────────────────────────

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra(EXTRA_TEXT) ?: ""
            when (intent?.action) {
                ACTION_SCREENSHOT  -> takeScreenshot()
                ACTION_LOCK        -> lockScreen()
                ACTION_GO_HOME     -> goHome()
                ACTION_GO_BACK     -> goBack()
                ACTION_GO_RECENTS  -> goToRecents()
                ACTION_SCROLL_UP   -> scrollUp()
                ACTION_SCROLL_DOWN -> scrollDown()
                ACTION_CLICK_TEXT  -> clickOnText(text)
                ACTION_TYPE_TEXT   -> typeText(text)
            }
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "✅ Accessibility Service connected")

        val filter = IntentFilter().apply {
            addAction(ACTION_SCREENSHOT)
            addAction(ACTION_LOCK)
            addAction(ACTION_GO_HOME)
            addAction(ACTION_GO_BACK)
            addAction(ACTION_GO_RECENTS)
            addAction(ACTION_SCROLL_UP)
            addAction(ACTION_SCROLL_DOWN)
            addAction(ACTION_CLICK_TEXT)
            addAction(ACTION_TYPE_TEXT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(actionReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(actionReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Track currently active app package for context-aware commands
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg != currentPackage && pkg != packageName) {
                currentPackage = pkg
                Log.d(TAG, "Current app → $pkg")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(actionReceiver) } catch (_: Exception) {}
        instance = null
        Log.d(TAG, "Accessibility Service destroyed")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Global Navigation Actions
    // ══════════════════════════════════════════════════════════════════════════

    fun goBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        Log.d(TAG, "Action: BACK")
    }

    fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
        Log.d(TAG, "Action: HOME")
    }

    fun goToRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        Log.d(TAG, "Action: RECENTS")
    }

    fun takeScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            Log.d(TAG, "Action: TAKE_SCREENSHOT")
        } else {
            Log.w(TAG, "Screenshot requires Android 9+")
        }
    }

    fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            Log.d(TAG, "Action: LOCK_SCREEN")
        } else {
            Log.w(TAG, "Lock screen requires Android 9+")
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI Interaction — Click, Type, Scroll
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Finds a node containing [text] and clicks it (or its nearest clickable parent).
     * Returns true if a click was performed.
     */
    fun clickOnText(text: String): Boolean {
        if (text.isBlank()) return false
        val root = rootInActiveWindow ?: run {
            Log.w(TAG, "clickOnText: rootInActiveWindow is null")
            return false
        }
        val nodes = root.findAccessibilityNodeInfosByText(text)
        if (nodes.isNullOrEmpty()) {
            Log.w(TAG, "clickOnText: no nodes found for \"$text\"")
            return false
        }

        for (node in nodes) {
            if (performClickOnNode(node)) return true
        }
        Log.w(TAG, "clickOnText: found ${nodes.size} node(s) but none were clickable for \"$text\"")
        return false
    }

    /**
     * Clicks a node — walks up the parent chain (up to 6 levels) to find a clickable ancestor.
     */
    private fun performClickOnNode(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Clicked node: ${node.text}")
            return true
        }
        var parent = node.parent
        var depth = 0
        while (parent != null && depth < 6) {
            if (parent.isClickable) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Clicked parent of node at depth $depth")
                return true
            }
            parent = parent.parent
            depth++
        }
        return false
    }

    /**
     * Types [text] into the currently focused editable field.
     * Returns true if text was inserted.
     */
    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val editNode = findEditableNode(root) ?: run {
            Log.w(TAG, "typeText: no editable field on screen")
            return false
        }
        // Focus first
        editNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val success = editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.d(TAG, "typeText \"$text\" → $success")
        return success
    }

    /**
     * Scrolls the active window forward (down).
     */
    fun scrollDown(): Boolean {
        val root = rootInActiveWindow ?: return false
        // Try the root first, then find the first scrollable child
        if (root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true
        val scrollable = findScrollableNode(root)
        val result = scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        Log.d(TAG, "scrollDown → $result")
        return result
    }

    /**
     * Scrolls the active window backward (up).
     */
    fun scrollUp(): Boolean {
        val root = rootInActiveWindow ?: return false
        if (root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) return true
        val scrollable = findScrollableNode(root)
        val result = scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
        Log.d(TAG, "scrollUp → $result")
        return result
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Gestures (Android 7+)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Performs a swipe-up gesture (bottom → top) — useful for scrolling or opening app drawer.
     */
    fun swipeUp(callback: ((Boolean) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            callback?.invoke(false)
            return
        }
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        val screenWidth  = resources.displayMetrics.widthPixels / 2f
        val path = Path().apply {
            moveTo(screenWidth, screenHeight * 0.75f)
            lineTo(screenWidth, screenHeight * 0.25f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 400))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                Log.d(TAG, "swipeUp ✔")
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription) {
                Log.w(TAG, "swipeUp ✘ cancelled")
                callback?.invoke(false)
            }
        }, null)
    }

    /**
     * Performs a swipe-down gesture (top → bottom) — useful for opening notification shade.
     */
    fun swipeDown(callback: ((Boolean) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            callback?.invoke(false)
            return
        }
        val screenWidth = resources.displayMetrics.widthPixels / 2f
        val path = Path().apply {
            moveTo(screenWidth, 50f)
            lineTo(screenWidth, 400f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                Log.d(TAG, "swipeDown ✔")
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription) {
                Log.w(TAG, "swipeDown ✘ cancelled")
                callback?.invoke(false)
            }
        }, null)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // App Launch Fallback via Launcher
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Opens an app by going HOME and clicking its launcher icon.
     * Use as fallback when [packageManager.getLaunchIntentForPackage] returns null.
     *
     * Flow: HOME → wait 900ms → try multiple label variants → click
     * If not found on home screen, swipes up to open app drawer and tries again.
     *
     * Label variants tried (in priority order):
     *  1. Full label (e.g. "WhatsApp Business")
     *  2. First word (e.g. "WhatsApp")
     *  3. Last word (e.g. "Business")
     *  4. Each individual word >= 4 chars
     */
    fun openAppViaLauncher(appLabel: String, onResult: (Boolean) -> Unit) {
        Log.d(TAG, "openAppViaLauncher: \"$appLabel\"")
        goHome()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (tryClickLabelVariants(appLabel)) {
                onResult(true)
                return@postDelayed
            }
            // Not on home screen — swipe up to reveal the app drawer then try again
            swipeUp {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onResult(tryClickLabelVariants(appLabel))
                }, 700)
            }
        }, 900)
    }

    /**
     * Builds a prioritized list of label variants and clicks the first one found on screen.
     * Returns true if any variant was clicked successfully.
     */
    private fun tryClickLabelVariants(appLabel: String): Boolean {
        val words = appLabel.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val variants = buildList {
            add(appLabel)                               // "WhatsApp Business"
            if (words.size > 1) add(words.first())     // "WhatsApp"
            if (words.size > 1) add(words.last())      // "Business"
            words.filter { it.length >= 4 }.forEach { add(it) }  // each long word
        }.distinct()

        for (variant in variants) {
            Log.d(TAG, "Trying label variant: \"$variant\"")
            if (clickOnText(variant)) {
                Log.d(TAG, "openAppViaLauncher: clicked \"$variant\"")
                return true
            }
        }
        Log.w(TAG, "openAppViaLauncher: no variant matched for \"$appLabel\" (tried: $variants)")
        return false
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }
}
