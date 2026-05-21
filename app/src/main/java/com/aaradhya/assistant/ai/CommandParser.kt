package com.aaradhya.assistant.ai

import com.aaradhya.assistant.model.AppCommand

/**
 * Parses transcribed voice text (Hinglish + English) into AppCommand objects.
 * Returns null if the text doesn't match any known command pattern.
 *
 * Priority order (highest → lowest):
 *  1. System toggles (volume, torch, wifi, bluetooth)
 *  2. Calling
 *  3. Alarm / Timer
 *  4. Settings navigation
 *  5. Media (music, YouTube)
 *  6. Navigation / Maps
 *  7. Web search / Play Store
 *  8. Screenshot
 *  9. Messaging (WhatsApp, SMS, Prime)
 * 10. Open App (broadest — last resort)
 */
object CommandParser {

    fun parse(text: String): AppCommand? {
        var t = text.trim().lowercase().replace(Regex("[.,!?]"), "")

        // Strip wake-word prefix
        t = t.replace(Regex("^(hey|hi|hello)?\\s*aaradhya\\s+"), "")

        // ── Volume ──────────────────────────────────────────────────────────────
        if (matchesAny(t, "volume badhao", "volume up", "awaaz badhao", "sound up",
                "louder", "volume increase", "volume badha", "awaaz badha")) {
            return AppCommand(AppCommand.VOLUME_UP)
        }
        if (matchesAny(t, "volume kam karo", "volume down", "awaaz kam karo", "sound down",
                "quieter", "volume decrease", "volume kam", "awaaz kam")) {
            return AppCommand(AppCommand.VOLUME_DOWN)
        }
        if (matchesAny(t, "mute karo", "mute kar", "silent karo", "silent mode",
                "phone mute", "awaaz band", "silent kar")) {
            return AppCommand(AppCommand.VOLUME_MUTE)
        }

        // ── Flashlight ─────────────────────────────────────────────────────────
        if (matchesAny(t, "torch on", "flashlight on", "torch on karo", "torch chalu karo",
                "light on karo", "torch laga", "torch jala", "light on")) {
            return AppCommand(AppCommand.FLASHLIGHT_ON)
        }
        if (matchesAny(t, "torch off", "flashlight off", "torch off karo", "torch band karo",
                "light off karo", "torch hata", "torch bujha", "light off")) {
            return AppCommand(AppCommand.FLASHLIGHT_OFF)
        }

        // ── WiFi ───────────────────────────────────────────────────────────────
        if (matchesAny(t, "wifi on", "wifi on karo", "wifi chalu karo", "wi-fi on",
                "wifi chalu", "internet on")) {
            return AppCommand(AppCommand.WIFI_ON)
        }
        if (matchesAny(t, "wifi off", "wifi off karo", "wifi band karo", "wi-fi off",
                "wifi band", "internet off")) {
            return AppCommand(AppCommand.WIFI_OFF)
        }

        // ── Bluetooth ──────────────────────────────────────────────────────────
        if (matchesAny(t, "bluetooth on", "bluetooth on karo", "bluetooth chalu karo",
                "bt on", "bluetooth chalu")) {
            return AppCommand(AppCommand.BT_ON)
        }
        if (matchesAny(t, "bluetooth off", "bluetooth off karo", "bluetooth band karo",
                "bt off", "bluetooth band")) {
            return AppCommand(AppCommand.BT_OFF)
        }

        // ── Settings Navigation ─────────────────────────────────────────────────
        if (matchesAny(t, "wifi settings", "wifi setting", "wifi settings kholo",
                "wifi settings open", "wifi setting kholo")) {
            return AppCommand(AppCommand.OPEN_WIFI_SETTINGS)
        }
        if (matchesAny(t, "bluetooth settings", "bluetooth setting", "bt settings",
                "bluetooth settings kholo", "bt setting kholo")) {
            return AppCommand(AppCommand.OPEN_BT_SETTINGS)
        }
        if (matchesAny(t, "battery settings", "battery setting", "battery saver",
                "battery settings kholo", "battery ka setting")) {
            return AppCommand(AppCommand.OPEN_BATTERY_SETTINGS)
        }
        if (matchesAny(t, "settings kholo", "settings open karo", "settings chalo",
                "phone settings", "open settings", "settings kholdo")) {
            return AppCommand(AppCommand.OPEN_SETTINGS)
        }

        // ── Calling ─────────────────────────────────────────────────────────────
        // "call [name]" / "[name] ko call karo" / "dial [name]" / "ring [name]"
        val callRegex = Regex(
            "(?:call|dial|ring|phone karo|phone kar|call karo|call kar)\\s+(.+)" +
            "|(.+?)\\s+ko\\s+(?:call|phone|ring|dial)\\s*(?:karo|kar|lagao|laga)?" +
            "|(.+?)\\s+ko\\s+call"
        )
        callRegex.find(t)?.let { m ->
            val name = (m.groupValues[1] + m.groupValues[2] + m.groupValues[3])
                .replace(Regex("\\b(now|please|bro|aaradhya|abhi|jaldi)\\b"), "")
                .trim()
            if (name.isNotEmpty()) return AppCommand(AppCommand.MAKE_CALL, mapOf("name" to name))
        }

        // ── Alarm ──────────────────────────────────────────────────────────────
        // "alarm 7 baje", "set alarm for 7:30", "alarm lagao 8 baje", "kal subah 7 baje alarm"
        val alarmRegex = Regex(
            "(?:alarm|alarm lagao|set alarm|alarm set karo)\\s+(?:for\\s+)?(?:kal\\s+)?(?:subah\\s+|raat\\s+|dopahar\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(?:baje|am|pm|a\\.m|p\\.m|bajey|o'clock)?" +
            "|(\\d{1,2})(?::(\\d{2}))?\\s*(?:baje|am|pm)\\s+(?:ka\\s+)?alarm"
        )
        alarmRegex.find(t)?.let { m ->
            val hour   = (m.groupValues[1].ifEmpty { m.groupValues[3] }).toIntOrNull() ?: return@let
            val minute = (m.groupValues[2].ifEmpty { m.groupValues[4].ifEmpty { "0" } }).toIntOrNull() ?: 0
            val isPm   = t.contains("pm") || t.contains("p.m") || t.contains("sham") || t.contains("raat")
            val finalHour = if (isPm && hour < 12) hour + 12 else if (!isPm && hour == 12) 0 else hour
            return AppCommand(AppCommand.SET_ALARM, mapOf(
                "hour" to finalHour.toString(),
                "minute" to minute.toString(),
                "label" to "Aaradhya Alarm"
            ))
        }
        // Simple fallback: "alarm lagao"
        if (matchesAny(t, "alarm lagao", "alarm set karo", "wake me up", "alarm laga")) {
            return AppCommand(AppCommand.SET_ALARM, mapOf("hour" to "-1", "minute" to "0", "label" to "Aaradhya Alarm"))
        }

        // ── Timer ──────────────────────────────────────────────────────────────
        // "5 minute ka timer", "timer lagao 10 minutes", "set timer for 30 seconds"
        val timerRegex = Regex(
            "(?:timer|timer lagao|set timer)\\s+(?:for\\s+)?(\\d+)\\s*(minute|min|second|sec|ghante|hour)" +
            "|(\\d+)\\s*(minute|min|second|sec|ghante|hour)\\s+(?:ka\\s+)?timer"
        )
        timerRegex.find(t)?.let { m ->
            val amount = (m.groupValues[1].ifEmpty { m.groupValues[3] }).toIntOrNull() ?: return@let
            val unit   = (m.groupValues[2].ifEmpty { m.groupValues[4] }).lowercase()
            val seconds = when {
                unit.startsWith("sec") -> amount
                unit.startsWith("min") -> amount * 60
                unit.startsWith("hour") || unit.startsWith("ghant") -> amount * 3600
                else -> amount * 60
            }
            return AppCommand(AppCommand.SET_TIMER, mapOf(
                "seconds" to seconds.toString(),
                "label" to "$amount $unit timer"
            ))
        }

        // ── Music ──────────────────────────────────────────────────────────────
        // "play music", "gaana chalaao", "music chalaao", "play [song name]"
        val musicQueryRegex = Regex("play\\s+(.+)")
        musicQueryRegex.find(t)?.let { m ->
            val query = m.groupValues[1].trim()
            // If it's a generic "play music", route to PLAY_MUSIC; if it has a query, check for YouTube
            if (query == "music" || query == "gaana" || query == "song" || query == "songs") {
                return AppCommand(AppCommand.PLAY_MUSIC, mapOf("query" to ""))
            }
            // "play [song] on youtube" → PLAY_YOUTUBE
            if (t.contains("youtube") || t.contains("yt")) {
                val cleanQuery = query.replace(Regex("\\bon\\s+youtube\\b|\\bon\\s+yt\\b"), "").trim()
                return AppCommand(AppCommand.PLAY_YOUTUBE, mapOf("query" to cleanQuery))
            }
            // Generic play — open music with query
            return AppCommand(AppCommand.PLAY_MUSIC, mapOf("query" to query))
        }
        if (matchesAny(t, "music chalaao", "music chala", "gaana bajao", "gaana chalaao",
                "music bajao", "song chalaao", "spotify chala", "gaana laga")) {
            return AppCommand(AppCommand.PLAY_MUSIC, mapOf("query" to ""))
        }

        // ── YouTube ────────────────────────────────────────────────────────────
        val ytRegex = Regex("(?:youtube|yt)\\s+(?:par\\s+|pe\\s+|on\\s+)?(?:search|dhundo|dekho)?\\s+(.+)")
        ytRegex.find(t)?.let { m ->
            val query = m.groupValues[1].trim()
            if (query.isNotEmpty()) return AppCommand(AppCommand.PLAY_YOUTUBE, mapOf("query" to query))
        }

        // ── Navigation / Maps ──────────────────────────────────────────────────
        // "navigate to [place]", "[place] le chalo", "[place] kaise jaun", "maps mein [place]"
        val navRegex = Regex(
            "(?:navigate|navigation|directions?|route)\\s+to\\s+(.+)" +
            "|(.+?)\\s+(?:le chalo|le ja|kaise jaun|ka rasta|directions chahiye|tak jaana)" +
            "|maps\\s+(?:mein|pe|par)\\s+(.+)" +
            "|(.+?)\\s+(?:map|maps)\\s+(?:mein|kholo|dekho)"
        )
        navRegex.find(t)?.let { m ->
            val destination = (m.groupValues[1] + m.groupValues[2] + m.groupValues[3] + m.groupValues[4])
                .replace(Regex("\\b(mein|pe|par|kholo|dekho)\\b"), "")
                .trim()
            if (destination.isNotEmpty()) {
                return AppCommand(AppCommand.NAVIGATE_TO, mapOf("destination" to destination))
            }
        }

        // ── Web Search ─────────────────────────────────────────────────────────
        // "search [query]", "google [query]", "[query] dhundo", "browser mein [query]"
        val webSearchRegex = Regex(
            "(?:search|google|bing|look up|find|dhundo)\\s+(?:for\\s+)?(.+)" +
            "|(.+?)\\s+(?:dhundo|search karo|google karo)" +
            "|browser\\s+(?:mein|pe)\\s+(.+)"
        )
        webSearchRegex.find(t)?.let { m ->
            val query = (m.groupValues[1] + m.groupValues[2] + m.groupValues[3])
                .replace(Regex("\\b(mein|pe|par|karo)\\b"), "")
                .trim()
            if (query.isNotEmpty() && query.length > 2) {
                return AppCommand(AppCommand.SEARCH_WEB, mapOf("query" to query))
            }
        }

        // ── Play Store ─────────────────────────────────────────────────────────
        if (matchesAny(t, "play store kholo", "play store open", "open play store",
                "play store chalo", "app download karo", "app store")) {
            val appSearchRegex = Regex("(?:play store mein|play store pe)\\s+(.+)\\s+(?:dhundo|search)")
            val appQuery = appSearchRegex.find(t)?.groupValues?.get(1)?.trim() ?: ""
            return AppCommand(AppCommand.OPEN_PLAY_STORE, mapOf("app" to appQuery))
        }

        // ── Screenshot & Global Actions ──────────────────────────────────────
        if (matchesAny(t, "screenshot lo", "screenshot le", "take screenshot",
                "screenshot lena", "capture screen", "screen capture", "screenshot")) {
            return AppCommand(AppCommand.TAKE_SCREENSHOT)
        }
        if (matchesAny(t, "lock screen", "lock phone", "screen lock", "screen lock karo",
                "phone lock karo", "phone lock kar", "lock kar", "screen lock kar", "lock window")) {
            return AppCommand(AppCommand.LOCK_SCREEN)
        }
        if (matchesAny(t, "go home", "home screen", "home pe chalo", "home screen kholo",
                "home jao", "go to home", "home chalo", "home kholdo", "home screen open")) {
            return AppCommand(AppCommand.GO_HOME)
        }
        if (matchesAny(t, "go back", "back jao", "peeche chalo", "back karo", "back", "piche chalo", "back kar")) {
            return AppCommand(AppCommand.GO_BACK)
        }

        // ── Prime contacts ─────────────────────────────────────────────────────
        if (matchesAny(t, "prime message 1", "prime message one", "prime msg 1", "prime msg one",
                "close friend ko message", "meri jaan ko msg karo", "love ko message",
                "close friend msg", "best friend ko message", "mere close friend ko message")) {
            return AppCommand(AppCommand.PRIME_MSG, mapOf("index" to "0"))
        }
        if (matchesAny(t, "prime message 2", "prime message two", "prime msg 2", "prime msg two")) {
            return AppCommand(AppCommand.PRIME_MSG, mapOf("index" to "1"))
        }

        // ── WhatsApp ───────────────────────────────────────────────────────────
        val whatsappMsgRegex = Regex("(.+?)\\s+ko\\s+whatsapp|whatsapp\\s+karo\\s+(.+)|whatsapp\\s+message\\s+(.+)")
        whatsappMsgRegex.find(t)?.let { m ->
            val name = (m.groupValues[1] + m.groupValues[2] + m.groupValues[3]).trim()
            if (name.isNotEmpty()) return AppCommand(AppCommand.WHATSAPP_MSG, mapOf("name" to name))
        }

        // ── SMS ────────────────────────────────────────────────────────────────
        val smsRegex = Regex("(.+?)\\s+ko\\s+(?:sms|message|msg)\\s+bhejo|send\\s+(?:sms|message)\\s+to\\s+(.+)")
        smsRegex.find(t)?.let { m ->
            val name = (m.groupValues[1] + m.groupValues[2]).trim()
            if (name.isNotEmpty()) return AppCommand(AppCommand.SMS, mapOf("name" to name))
        }

        // ── Recents ────────────────────────────────────────────────────────────
        if (matchesAny(t, "recent apps", "recents kholo", "recent apps dikhao",
                "app switcher", "recent kholo", "recent apps open")) {
            return AppCommand(AppCommand.OPEN_RECENTS)
        }

        // ── Scroll ─────────────────────────────────────────────────────────────
        if (matchesAny(t, "scroll up", "upar scroll", "scroll karo upar", "scroll up karo",
                "upar jao", "page upar")) {
            return AppCommand(AppCommand.SCROLL_UP)
        }
        if (matchesAny(t, "scroll down", "neeche scroll", "scroll karo neeche", "scroll down karo",
                "neeche jao", "page neeche")) {
            return AppCommand(AppCommand.SCROLL_DOWN)
        }

        // ── Click text on screen ───────────────────────────────────────────────
        // "click [text]", "[text] pe click karo", "[text] tap karo"
        val clickRegex = Regex("(?:click|tap|press)\\s+(?:on\\s+)?(.+)|(.+?)\\s+(?:pe|par)\\s+(?:click|tap|press)\\s*(?:karo|kar)?")
        clickRegex.find(t)?.let { m ->
            val target = (m.groupValues[1] + m.groupValues[2]).trim()
            if (target.isNotEmpty() && target.length > 1) {
                return AppCommand(AppCommand.CLICK_TEXT, mapOf("text" to target))
            }
        }

        // ── Type text ──────────────────────────────────────────────────────────
        // "type [text]", "[text] type karo", "likhdo [text]"
        val typeRegex = Regex("(?:type|write|likho|likhdo|type karo)\\s+(.+)|(.+?)\\s+(?:type karo|likho|likhdo)")
        typeRegex.find(t)?.let { m ->
            val text = (m.groupValues[1] + m.groupValues[2]).trim()
            if (text.isNotEmpty()) return AppCommand(AppCommand.TYPE_TEXT, mapOf("text" to text))
        }

        // ── Open App ──────────────────────────────────────────────────────────
        // 1. Structured fallback (if Gemini somehow still outputs it)
        if (t.startsWith("open_app")) {
            val app = t.removePrefix("open_app").removePrefix(":").trim()
            if (app.isNotEmpty()) return AppCommand(AppCommand.OPEN_APP, mapOf("app" to app))
        }

        // 2. English prefix (e.g., "can you open youtube", "launch spotify now")
        val enRegex = Regex("\\b(?:open|opening|launch|launching|start|starting)\\s+(.+)").find(t)
        if (enRegex != null) {
            var app = enRegex.groupValues[1]
                .replace(Regex("\\b(now|please|bro|aaradhya)\\b"), "")
                .trim()
            if (app.isNotEmpty()) return AppCommand(AppCommand.OPEN_APP, mapOf("app" to app))
        }

        // 3. Hinglish suffix (e.g., "mera youtube kholo", "spotify chala do")
        val hiRegex = Regex("(.+?)\\s+(?:kholo|khol|chalo|chala\\b|open karo|open kar\\b)").find(t)
        if (hiRegex != null) {
            var app = hiRegex.groupValues[1]
                .replace(Regex("^(haan|acha|ok|okay)\\s+"), "")
                .trim()
            if (app.isNotEmpty()) return AppCommand(AppCommand.OPEN_APP, mapOf("app" to app))
        }

        return null
    }

    private fun matchesAny(text: String, vararg patterns: String): Boolean {
        return patterns.any { text.contains(it) }
    }
}
