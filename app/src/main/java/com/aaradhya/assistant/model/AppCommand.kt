package com.aaradhya.assistant.model

/**
 * Represents a parsed voice or text command from the user.
 * @param type The command type identifier (e.g., "SMS", "VOLUME_UP", etc.)
 * @param params Additional parameters for the command
 */
data class AppCommand(
    val type: String,
    val params: Map<String, String> = emptyMap()
) {
    companion object {
        // ── App Control ────────────────────────────────────────────────────────
        const val OPEN_APP          = "OPEN_APP"

        // ── Messaging ─────────────────────────────────────────────────────────
        const val SMS               = "SMS"
        const val WHATSAPP_MSG      = "WHATSAPP_MSG"
        const val PRIME_MSG         = "PRIME_MSG"

        // ── Calling ───────────────────────────────────────────────────────────
        const val MAKE_CALL         = "MAKE_CALL"       // params: "name" or "number"

        // ── Volume ────────────────────────────────────────────────────────────
        const val VOLUME_UP         = "VOLUME_UP"
        const val VOLUME_DOWN       = "VOLUME_DOWN"
        const val VOLUME_MUTE       = "VOLUME_MUTE"

        // ── Hardware Toggles ──────────────────────────────────────────────────
        const val FLASHLIGHT_ON     = "FLASHLIGHT_ON"
        const val FLASHLIGHT_OFF    = "FLASHLIGHT_OFF"
        const val WIFI_ON           = "WIFI_ON"
        const val WIFI_OFF          = "WIFI_OFF"
        const val BT_ON             = "BLUETOOTH_ON"
        const val BT_OFF            = "BLUETOOTH_OFF"

        // ── System Settings Navigation ────────────────────────────────────────
        const val OPEN_SETTINGS         = "OPEN_SETTINGS"
        const val OPEN_WIFI_SETTINGS    = "OPEN_WIFI_SETTINGS"
        const val OPEN_BT_SETTINGS      = "OPEN_BT_SETTINGS"
        const val OPEN_BATTERY_SETTINGS = "OPEN_BATTERY_SETTINGS"

        // ── Alarms & Reminders ────────────────────────────────────────────────
        const val SET_ALARM         = "SET_ALARM"       // params: "hour", "minute", "label"
        const val SET_TIMER         = "SET_TIMER"       // params: "seconds", "label"

        // ── Media & Entertainment ─────────────────────────────────────────────
        const val PLAY_MUSIC        = "PLAY_MUSIC"      // params: "query" (optional)
        const val PLAY_YOUTUBE      = "PLAY_YOUTUBE"    // params: "query"

        // ── Navigation ────────────────────────────────────────────────────────
        const val NAVIGATE_TO       = "NAVIGATE_TO"     // params: "destination"

        // ── Web & Store ───────────────────────────────────────────────────────
        const val SEARCH_WEB        = "SEARCH_WEB"      // params: "query"
        const val OPEN_PLAY_STORE   = "OPEN_PLAY_STORE" // params: "app" (optional)

        // ── Screenshot & Global Actions ──────────────────────────────────────
        const val TAKE_SCREENSHOT   = "TAKE_SCREENSHOT"
        const val LOCK_SCREEN       = "LOCK_SCREEN"
        const val GO_HOME           = "GO_HOME"
        const val GO_BACK           = "GO_BACK"
        const val OPEN_RECENTS      = "OPEN_RECENTS"

        // ── Accessibility UI Interactions ─────────────────────────────────────
        const val SCROLL_UP         = "SCROLL_UP"
        const val SCROLL_DOWN       = "SCROLL_DOWN"
        const val CLICK_TEXT        = "CLICK_TEXT"   // params: "text"
        const val TYPE_TEXT         = "TYPE_TEXT"    // params: "text"
    }
}
