package com.aaradhya.assistant.viewmodel

import android.app.Application
import android.content.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaradhya.assistant.apps.InstalledAppsManager
import com.aaradhya.assistant.apps.KnownApps
import com.aaradhya.assistant.model.AppCommand
import com.aaradhya.assistant.service.AccessibilityHelperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Central command executor for AARADHYA.
 *
 * Hybrid architecture:
 *  1. Normal Android Intent (primary)
 *  2. AccessibilityHelperService (fallback + UI interactions)
 *
 * Exposes [commandResult] LiveData — MainActivity forwards this back to Gemini as speech.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val commandResult = MutableLiveData<String?>()
    val actionIntent  = MutableLiveData<Intent>()
    var intentExecutor: ((Intent) -> Unit)? = null

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun cleanParam(value: String?): String =
        value?.trim()?.lowercase()
            ?.replace(Regex("[.,!?]+$"), "")
            ?.trim() ?: ""

    // ── Execute Command ────────────────────────────────────────────────────────

    fun executeCommand(command: AppCommand, onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = when (command.type) {

                // App & media
                AppCommand.OPEN_APP          -> launchApp(cleanParam(command.params["app"]))
                AppCommand.PLAY_MUSIC        -> playMusic(cleanParam(command.params["query"]))
                AppCommand.PLAY_YOUTUBE      -> playYoutube(cleanParam(command.params["query"]))

                // Messaging
                AppCommand.SMS               -> sendSms(cleanParam(command.params["name"]), command.params["message"] ?: "")
                AppCommand.WHATSAPP_MSG      -> openWhatsApp(cleanParam(command.params["name"]))
                AppCommand.PRIME_MSG         -> primeMsg(command.params["index"]?.toIntOrNull() ?: 0)

                // Calling
                AppCommand.MAKE_CALL         -> makeCall(
                    cleanParam(command.params["name"]),
                    cleanParam(command.params["number"])
                )

                // Volume
                AppCommand.VOLUME_UP         -> adjustVolume(true)
                AppCommand.VOLUME_DOWN       -> adjustVolume(false)
                AppCommand.VOLUME_MUTE       -> muteDevice()

                // Hardware toggles
                AppCommand.FLASHLIGHT_ON     -> toggleFlashlight(true)
                AppCommand.FLASHLIGHT_OFF    -> toggleFlashlight(false)
                AppCommand.WIFI_ON           -> toggleWifi(true)
                AppCommand.WIFI_OFF          -> toggleWifi(false)
                AppCommand.BT_ON             -> toggleBluetooth(true)
                AppCommand.BT_OFF            -> toggleBluetooth(false)

                // Settings navigation
                AppCommand.OPEN_SETTINGS         -> openSystemSettings()
                AppCommand.OPEN_WIFI_SETTINGS    -> openWifiSettings()
                AppCommand.OPEN_BT_SETTINGS      -> openBluetoothSettings()
                AppCommand.OPEN_BATTERY_SETTINGS -> openBatterySettings()

                // Alarms & timers
                AppCommand.SET_ALARM         -> setAlarm(
                    command.params["hour"]?.toIntOrNull() ?: -1,
                    command.params["minute"]?.toIntOrNull() ?: 0,
                    command.params["label"] ?: "Aaradhya Alarm"
                )
                AppCommand.SET_TIMER         -> setTimer(
                    command.params["seconds"]?.toIntOrNull() ?: 60,
                    command.params["label"] ?: "Aaradhya Timer"
                )

                // Navigation
                AppCommand.NAVIGATE_TO       -> navigateTo(cleanParam(command.params["destination"]))

                // Web & store
                AppCommand.SEARCH_WEB        -> searchWeb(cleanParam(command.params["query"]))
                AppCommand.OPEN_PLAY_STORE   -> openPlayStore(cleanParam(command.params["app"]))

                // Screenshot & Global Actions (via Accessibility)
                AppCommand.TAKE_SCREENSHOT   -> takeScreenshot()
                AppCommand.LOCK_SCREEN       -> lockScreen()
                AppCommand.GO_HOME           -> goHome()
                AppCommand.GO_BACK           -> goBack()
                AppCommand.OPEN_RECENTS      -> openRecents()

                // Accessibility UI Interactions
                AppCommand.SCROLL_UP         -> accessibilityScroll(up = true)
                AppCommand.SCROLL_DOWN       -> accessibilityScroll(up = false)
                AppCommand.CLICK_TEXT        -> accessibilityClickText(command.params["text"] ?: "")
                AppCommand.TYPE_TEXT         -> accessibilityTypeText(command.params["text"] ?: "")

                else -> "Unknown command: ${command.type}"
            }

            if (onResult != null) onResult.invoke(result)
            else commandResult.postValue(result)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Intent Launcher — routes through ActivityOptions on Android 14+
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun fireIntent(intent: Intent): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

            // ── Preferred: route through MainActivity.startActivity() (bulletproof on all Android versions) ──
            val executor = intentExecutor
            if (executor != null) {
                executor.invoke(intent)
                Log.d("MainViewModel", "fireIntent via intentExecutor OK  action=${intent.action} pkg=${intent.`package`}")
                return@withContext true
            }

            // ── Fallback: direct context launch with Android 14 ActivityOptions ──
            val ctx = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = android.app.ActivityOptions.makeBasic().apply {
                    pendingIntentBackgroundActivityStartMode =
                        android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
                ctx.startActivity(intent, options.toBundle())
            } else {
                ctx.startActivity(intent)
            }

            Log.d("MainViewModel", "fireIntent OK  action=${intent.action} pkg=${intent.`package`} data=${intent.data}")
            true
        } catch (e: Exception) {
            Log.e("MainViewModel", "fireIntent FAIL  ${e.message}", e)
            false
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // App Launch — Intent first, then Accessibility launcher fallback
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun launchApp(appName: String): String {
        if (appName.isBlank()) return "Kaunsa app kholun?"

        val ctx = getApplication<Application>()
        val pm  = ctx.packageManager
        Log.d("APP_LAUNCH", "Requested: \"$appName\"")

        // ── Special handling: Camera ──────────────────────────────────────────
        if (appName.contains("camera")) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            return if (fireIntent(intent)) "Camera khol diya!" else "Camera open nahi hua."
        }

        // ── Tier 1: KnownApps fast-path (package name map, most reliable) ────
        val knownPkg = KnownApps.resolvePackage(appName)
        if (knownPkg != null) {
            Log.d("APP_LAUNCH", "KnownApps hit: \"$appName\" → $knownPkg")
            val knownIntent = pm.getLaunchIntentForPackage(knownPkg)
            if (knownIntent != null && fireIntent(knownIntent)) {
                val label = try { pm.getApplicationLabel(pm.getApplicationInfo(knownPkg, 0)).toString() } catch (_: Exception) { appName }
                return "$label khol diya!"
            }
            // Package known but not installed — fall through to scanner
            Log.w("APP_LAUNCH", "KnownApps package $knownPkg not installed — falling back to scanner")
        }

        // ── Tier 2: Dynamic scan + fuzzy label match ──────────────────────────
        val matchedApp = InstalledAppsManager.findApp(ctx, appName)
        if (matchedApp == null) {
            Log.w("APP_LAUNCH", "No match for \"$appName\" — installed: ${InstalledAppsManager.getAllApps(ctx).take(10).map { it.label }}")
            return "\"$appName\" install nahi hai ya mujhe nahi mila."
        }

        Log.d("APP_LAUNCH", "Scanner matched: \"$appName\" → ${matchedApp.label} (${matchedApp.packageName})")

        // ── Tier 3: getLaunchIntentForPackage (standard launch) ───────────────
        val intent = pm.getLaunchIntentForPackage(matchedApp.packageName)
        if (intent != null && fireIntent(intent)) {
            Log.d("APP_LAUNCH", "Intent launch OK: ${matchedApp.packageName}")
            return "${matchedApp.label} khol diya!"
        }

        // ── Tier 4: Accessibility Launcher fallback (HOME → icon → click) ─────
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            Log.d("APP_LAUNCH", "Intent failed — trying Accessibility launcher for ${matchedApp.label}")
            val success = suspendCancellableCoroutine<Boolean> { cont ->
                svc.openAppViaLauncher(matchedApp.label) { result ->
                    if (cont.isActive) cont.resume(result)
                }
            }
            return if (success) "${matchedApp.label} khol diya (launcher se)!"
            else "${matchedApp.label} open nahi hua. Launcher mein icon nahi mila."
        }

        return "Sorry, ${matchedApp.label} open nahi hua. Accessibility service enable karo!"
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Music & YouTube
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun playMusic(query: String): String {
        val ctx = getApplication<Application>()
        val musicApps = listOf("spotify", "youtube music", "gaana", "jiosaavn", "wynk",
            "amazon music", "apple music", "soundcloud")
        for (appName in musicApps) {
            val intent = InstalledAppsManager.getLaunchIntent(ctx, appName)
            if (intent != null) {
                return if (fireIntent(intent)) "Music chala diya!" else continue
            }
        }
        val intent = Intent(Intent.ACTION_VIEW).apply { type = "audio/*" }
        return if (fireIntent(intent)) "Music app khol diya!"
        else "Koi music app nahi mila. Spotify ya Gaana install karo!"
    }

    private suspend fun playYoutube(query: String): String {
        if (query.isBlank()) return launchApp("youtube")
        val youtubeSearch = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", query)
        }
        val ctx = getApplication<Application>()
        if (youtubeSearch.resolveActivity(ctx.packageManager) != null) {
            return if (fireIntent(youtubeSearch)) "YouTube pe \"$query\" search kar raha hoon!"
            else openYoutubeFallback(query)
        }
        return openYoutubeFallback(query)
    }

    private suspend fun openYoutubeFallback(query: String): String {
        val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return if (fireIntent(intent)) "YouTube pe \"$query\" search kar raha hoon!"
        else "YouTube open nahi hua."
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Messaging
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun sendSms(name: String, message: String): String {
        val number = lookupContactNumber(name) ?: name
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$number")).apply {
            putExtra("sms_body", message)
        }
        return if (fireIntent(intent)) "$name ko message bhej diya!"
        else "Sorry, SMS open nahi ho paaya."
    }

    private suspend fun openWhatsApp(name: String): String {
        val number  = lookupContactNumber(name) ?: name
        val cleaned = number.replace(Regex("[^\\d+]"), "")
        val url     = "https://wa.me/$cleaned?text=Hey!"
        val intent  = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return if (fireIntent(intent)) "WhatsApp message kar rahi hoon $name ko!"
        else "Sorry, WhatsApp open nahi ho paaya."
    }

    // ── Prime Contacts ──────────────────────────────────────────────────────────

    fun getPrimeContacts(): List<Pair<String, String>> {
        val ctx   = getApplication<Application>()
        val prefs = ctx.getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
        val json  = prefs.getString("prime_contacts_json", null)
        if (json != null) {
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).mapNotNull { i ->
                    val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                    val n   = obj.optString("name")
                    val num = obj.optString("number")
                    if (n.isNotEmpty() && num.isNotEmpty()) Pair(n, num) else null
                }
            } catch (e: Exception) { emptyList() }
        }
        val name   = prefs.getString("prime_name", null)
        val number = prefs.getString("prime_number", null)
        return if (name != null && number != null) listOf(Pair(name, number)) else emptyList()
    }

    fun savePrimeContacts(contacts: List<Pair<String, String>>) {
        val ctx   = getApplication<Application>()
        val prefs = ctx.getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
        val arr   = JSONArray()
        contacts.forEach { (name, number) ->
            arr.put(JSONObject().apply { put("name", name); put("number", number) })
        }
        prefs.edit().putString("prime_contacts_json", arr.toString()).apply()
    }

    private suspend fun primeMsg(index: Int): String {
        val contacts = getPrimeContacts()
        if (contacts.isEmpty() || index >= contacts.size) return "Prime contact set nahi hai."
        val (name, _) = contacts[index]
        return sendSms(name, "")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Calling
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun makeCall(name: String, directNumber: String): String {
        if (directNumber.isNotBlank() && directNumber.any { it.isDigit() }) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$directNumber"))
            return if (fireIntent(intent)) "$name ko call kar raha hoon!" else "Call nahi ho paya."
        }
        if (name.isBlank()) return "Kisko call karun?"
        val number = lookupContactNumber(name)
        if (number != null) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            return if (fireIntent(intent)) "$name ko call kar raha hoon!" else "Call nahi ho paya."
        }
        val dialIntent = Intent(Intent.ACTION_DIAL).apply { putExtra(Intent.EXTRA_TEXT, name) }
        return if (fireIntent(dialIntent)) "Contacts mein \"$name\" dhundh raha hoon..."
        else "\"$name\" contacts mein nahi mila."
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Volume
    // ══════════════════════════════════════════════════════════════════════════

    private fun adjustVolume(up: Boolean): String {
        val ctx = getApplication<Application>()
        val am  = ctx.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        am.adjustStreamVolume(
            android.media.AudioManager.STREAM_MUSIC,
            if (up) android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER,
            android.media.AudioManager.FLAG_SHOW_UI
        )
        return if (up) "Volume badhaya!" else "Volume kam kiya!"
    }

    private fun muteDevice(): String {
        val ctx = getApplication<Application>()
        val am  = ctx.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        am.adjustStreamVolume(
            android.media.AudioManager.STREAM_MUSIC,
            android.media.AudioManager.ADJUST_MUTE,
            android.media.AudioManager.FLAG_SHOW_UI
        )
        return "Phone mute kar diya!"
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Hardware Toggles
    // ══════════════════════════════════════════════════════════════════════════

    private fun toggleFlashlight(on: Boolean): String {
        val ctx = getApplication<Application>()
        return try {
            val cm       = ctx.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cm.cameraIdList.firstOrNull() ?: return "Camera nahi mila."
            cm.setTorchMode(cameraId, on)
            if (on) "Torch on!" else "Torch off!"
        } catch (e: Exception) {
            "Torch toggle nahi ho saka."
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun toggleWifi(on: Boolean): String {
        val ctx = getApplication<Application>()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_WIFI)
            if (fireIntent(intent)) "WiFi settings khol diya." else "WiFi settings nahi khuli."
        } else {
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.isWifiEnabled = on
            if (on) "WiFi on kar diya!" else "WiFi off kar diya!"
        }
    }

    private suspend fun toggleBluetooth(on: Boolean): String {
        val intent = if (on) Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
                     else    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        return if (fireIntent(intent)) {
            if (on) "Bluetooth on kar rahi hoon!" else "Bluetooth settings khol diya."
        } else "Bluetooth kholne mein error."
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Settings Navigation
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun openSystemSettings(): String {
        return if (fireIntent(Intent(Settings.ACTION_SETTINGS))) "Settings khol diya!"
        else "Settings nahi khuli."
    }

    private suspend fun openWifiSettings(): String {
        return if (fireIntent(Intent(Settings.ACTION_WIFI_SETTINGS))) "WiFi settings khol diya!"
        else "WiFi settings nahi khuli."
    }

    private suspend fun openBluetoothSettings(): String {
        return if (fireIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))) "Bluetooth settings khol diya!"
        else "Bluetooth settings nahi khuli."
    }

    private suspend fun openBatterySettings(): String {
        return if (fireIntent(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))) "Battery settings khol diya!"
        else "Battery settings nahi khuli."
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Alarms & Timers
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun setAlarm(hour: Int, minute: Int, label: String): String {
        if (hour == -1) {
            return if (fireIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS))) "Alarm app khol diya!"
            else "Clock app open nahi hua."
        }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        val timeStr = String.format("%02d:%02d", hour, minute)
        return if (fireIntent(intent)) "Alarm $timeStr baje ke liye set kar diya!"
        else "Alarm set nahi ho paya."
    }

    private suspend fun setTimer(seconds: Int, label: String): String {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        val minutes = seconds / 60
        val secs    = seconds % 60
        val timeStr = if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
        return if (fireIntent(intent)) "$timeStr ka timer set kar diya!"
        else "Timer set nahi ho paya."
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Navigation / Maps
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun navigateTo(destination: String): String {
        if (destination.isBlank()) return "Kahan jaana hai?"
        val uri    = Uri.parse("google.navigation:q=${Uri.encode(destination)}&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        val ctx = getApplication<Application>()
        return if (intent.resolveActivity(ctx.packageManager) != null && fireIntent(intent)) {
            "\"$destination\" ka route Google Maps mein khol diya!"
        } else {
            val browserUri    = Uri.parse("https://maps.google.com/maps?q=${Uri.encode(destination)}")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            if (fireIntent(browserIntent)) "\"$destination\" Maps mein khol diya!"
            else "Maps open nahi hua."
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Web Search & Play Store
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun searchWeb(query: String): String {
        if (query.isBlank()) return "Kya search karun?"
        val uri    = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return if (fireIntent(intent)) "Google pe \"$query\" search kar raha hoon!"
        else "Browser open nahi hua."
    }

    private suspend fun openPlayStore(appQuery: String): String {
        val intent = if (appQuery.isNotBlank()) {
            Intent(Intent.ACTION_VIEW,
                Uri.parse("market://search?q=${Uri.encode(appQuery)}&c=apps"))
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://"))
        }
        return if (fireIntent(intent)) {
            if (appQuery.isNotBlank()) "Play Store pe \"$appQuery\" dhundh raha hoon!"
            else "Play Store khol diya!"
        } else {
            val browserUri = if (appQuery.isNotBlank())
                "https://play.google.com/store/search?q=${Uri.encode(appQuery)}&c=apps"
            else "https://play.google.com/store"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUri))
            if (fireIntent(browserIntent)) "Play Store khol diya!"
            else "Play Store open nahi hua."
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Screenshot & Global Actions — use service instance directly when available
    // ══════════════════════════════════════════════════════════════════════════

    private fun takeScreenshot(): String {
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            svc.takeScreenshot()
        } else {
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(AccessibilityHelperService.ACTION_SCREENSHOT))
        }
        return "Screenshot le raha hoon!"
    }

    private fun lockScreen(): String {
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            svc.lockScreen()
        } else {
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(AccessibilityHelperService.ACTION_LOCK))
        }
        return "Phone lock kar diya!"
    }

    private fun goHome(): String {
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            svc.goHome()
        } else {
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(AccessibilityHelperService.ACTION_GO_HOME))
        }
        return "Home screen pe jaa rahi hoon!"
    }

    private fun goBack(): String {
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            svc.goBack()
        } else {
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(AccessibilityHelperService.ACTION_GO_BACK))
        }
        return "Back jaa rahi hoon!"
    }

    private fun openRecents(): String {
        val svc = AccessibilityHelperService.instance
        if (svc != null) {
            svc.goToRecents()
        } else {
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(AccessibilityHelperService.ACTION_GO_RECENTS))
        }
        return "Recent apps khol diya!"
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Accessibility UI Interactions
    // ══════════════════════════════════════════════════════════════════════════

    private fun accessibilityScroll(up: Boolean): String {
        val svc = AccessibilityHelperService.instance
            ?: return "Accessibility service enable nahi hai."
        return if (up) {
            if (svc.scrollUp()) "Upar scroll kiya!" else "Scroll nahi ho paya."
        } else {
            if (svc.scrollDown()) "Neeche scroll kiya!" else "Scroll nahi ho paya."
        }
    }

    private fun accessibilityClickText(text: String): String {
        if (text.isBlank()) return "Kya click karun?"
        val svc = AccessibilityHelperService.instance
            ?: return "Accessibility service enable nahi hai."
        return if (svc.clickOnText(text)) "\"$text\" click kar diya!"
        else "\"$text\" screen pe nahi mila."
    }

    private fun accessibilityTypeText(text: String): String {
        if (text.isBlank()) return "Kya type karun?"
        val svc = AccessibilityHelperService.instance
            ?: return "Accessibility service enable nahi hai."
        return if (svc.typeText(text)) "Type kar diya: \"$text\""
        else "Koi text field nahi mila type karne ke liye."
    }

    /** Returns whether Accessibility service is currently active */
    fun isAccessibilityEnabled(): Boolean {
        val ctx = getApplication<Application>()
        return AccessibilityHelperService.isEnabled(ctx)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Contact Lookup
    // ══════════════════════════════════════════════════════════════════════════

    fun lookupContactNumber(name: String): String? {
        val ctx       = getApplication<Application>()
        val lowerName = name.lowercase()
        val cursor    = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        ) ?: return null

        cursor.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx  = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val cName   = it.getString(nameIdx) ?: continue
                val cNumber = it.getString(numIdx)  ?: continue
                if (cName.lowercase().contains(lowerName)) return cNumber
            }
        }
        return null
    }
}
