package com.aaradhya.assistant.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaradhya.assistant.R
import com.aaradhya.assistant.ai.AudioEngine
import com.aaradhya.assistant.ai.CommandParser
import com.aaradhya.assistant.ai.GeminiLiveClient
import com.aaradhya.assistant.model.AppCommand
import com.aaradhya.assistant.service.AARADHYAOverlayService
import com.aaradhya.assistant.service.AccessibilityHelperService
import com.aaradhya.assistant.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ── ViewModel ──────────────────────────────────────────────────────────────
    private val viewModel: MainViewModel by viewModels()

    // ── AI + Audio ─────────────────────────────────────────────────────────────
    private var geminiLive:  GeminiLiveClient? = null
    private var audioEngine: AudioEngine? = null

    // ── UI Views ───────────────────────────────────────────────────────────────
    private lateinit var orbView:        OrbAnimationView
    private lateinit var waveformView:   WaveformView
    private lateinit var statusText:     TextView
    private var chatRecycler:   RecyclerView? = null
    private lateinit var micButton:      ImageButton
    private lateinit var settingsBtn:    ImageButton
    private lateinit var batteryText:    TextView
    private var ramText:        TextView? = null
    private lateinit var timeText:       TextView
    private lateinit var redOverlay:     View
    private var chatAdapter:    ChatAdapter? = null
    private var systemVitalsView: SystemVitalsView? = null

    // ── State ──────────────────────────────────────────────────────────────────
    private var isMuted        = false
    private var inputBuffer    = StringBuilder()
    private var outputBuffer   = StringBuilder()
    private var isFirstConnection = true
    private val conversationHistory = mutableListOf<String>()
    private var lastTurnHadFunctionCall = false

    // ── WakeLock — FIX: prevents CPU sleep killing audio session after 60-90s ──
    private var wakeLock: PowerManager.WakeLock? = null

    // ── Handlers ───────────────────────────────────────────────────────────────
    private val mainHandler = Handler(Looper.getMainLooper())
    private val statusRunnable = object : Runnable {
        override fun run() {
            updateStatusBar()
            mainHandler.postDelayed(this, 30_000)
        }
    }



    // ── Permissions ────────────────────────────────────────────────────────────
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        if (allGranted) {
            Log.d(TAG, "All permissions granted")
            startSystemServices()
        } else {
            Log.w(TAG, "Some permissions denied, starting anyway")
            startSystemServices()
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Transparent status & navigation
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = android.graphics.Color.TRANSPARENT
            navigationBarColor = android.graphics.Color.parseColor("#050505")
        }

        setupCrashProtector()

        setContentView(R.layout.activity_main)
        initViews()
        checkPermissions()

        // Observe command results
        viewModel.commandResult.observe(this) { result ->
            result?.let { geminiLive?.sendText(it) }
        }

        // Observe intent requests as a fallback
        viewModel.actionIntent.observe(this) { intent ->
            intent?.let {
                try {
                    Toast.makeText(this@MainActivity, "Launching app...", Toast.LENGTH_SHORT).show()
                    startActivity(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start activity via actionIntent: ${e.message}")
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Use intentExecutor for reliable intent launching directly on the UI thread
        viewModel.intentExecutor = { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        // Init Gemini after short delay
        mainHandler.postDelayed({ initGeminiLive() }, 300)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!isMuted) audioEngine?.isMuted = false
        // Start status bar updates
        statusRunnable.run()
    }

    override fun onPause() {
        super.onPause()
        // Stop status bar updates to save battery
        mainHandler.removeCallbacks(statusRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        geminiLive?.disconnect()
        audioEngine?.release()
        mainHandler.removeCallbacks(statusRunnable)
        // FIX: Release WakeLock so battery doesn't drain after app closes
        releaseWakeLock()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Init
    // ══════════════════════════════════════════════════════════════════════════

    private fun initViews() {
        orbView      = findViewById(R.id.orbView)
        waveformView = findViewById(R.id.waveformView)
        statusText   = findViewById(R.id.statusText)
        chatRecycler = findViewById(R.id.chatRecycler)
        micButton    = findViewById(R.id.micButton)
        settingsBtn  = findViewById(R.id.settingsBtn)
        batteryText  = findViewById(R.id.batteryText)
        timeText     = findViewById(R.id.timeText)
        redOverlay   = findViewById(R.id.redOverlay)
        systemVitalsView = findViewById(R.id.systemVitalsView)

        chatAdapter = ChatAdapter()
        chatRecycler?.adapter = chatAdapter
        chatRecycler?.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        // Mic button
        micButton.setOnClickListener { toggleMute() }
        micButton.setOnLongClickListener {
            interruptAaradhya()
            true
        }

        // Settings button
        settingsBtn.setOnClickListener {
            startActivity(Intent(this, com.aaradhya.assistant.ui.settings.SettingsActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val required = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionsLauncher.launch(missing.toTypedArray())
        } else {
            startSystemServices()
        }
    }

    private fun startSystemServices() {
        // Overlay service
        try {
            startForegroundService(Intent(this, AARADHYAOverlayService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "AARADHYAOverlayService start error: ${e.message}")
        }

        // Check if Accessibility Service is enabled — show prompt if not
        checkAccessibilityService()
    }

    private fun checkAccessibilityService() {
        if (!AccessibilityHelperService.isEnabled(this)) {
            Log.w(TAG, "Accessibility Service not enabled")
            mainHandler.postDelayed({
                // Show tip in chat
                chatAdapter?.addMessage(
                    ChatMessage(
                        "Tip: Settings > Accessibility > Aaradhya mein Accessibility Service enable karo — isse app launch, scroll aur UI control bahut better hoga!",
                        isUser = false
                    )
                )
                chatAdapter?.let { chatRecycler?.scrollToPosition(it.itemCount - 1) }
                // Also show a Toast
                Toast.makeText(
                    this,
                    "Accessibility Service enable karo for full power!",
                    Toast.LENGTH_LONG
                ).show()
            }, 3000) // Delay 3s so Gemini greeting comes first
        } else {
            Log.d(TAG, "Accessibility Service is enabled")
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Gemini Live
    // ══════════════════════════════════════════════════════════════════════════

    private fun initGeminiLive() {
        val prefs = getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
        val primaryApiKey = prefs.getString("api_key", "") ?: ""
        val secondaryApiKey = prefs.getString("api_key_secondary", "") ?: ""

        if (primaryApiKey.isEmpty()) {
            statusText.text = "⚙️ Settings mein API key daalo!"
            return
        }
        
        discoverModelAndConnect(primaryApiKey, secondaryApiKey, prefs, isRetry = false)
    }

    private fun discoverModelAndConnect(currentApiKey: String, fallbackApiKey: String, prefs: SharedPreferences, isRetry: Boolean) {
        statusText.text = if (isRetry) "Retrying with Secondary Key..." else "Finding Live Model..."

        // FIX: Use a named thread + proper per-response try-catch to avoid crash on malformed JSON
        val thread = Thread {
            try {
                val httpClient = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                var foundModel = ""
                var foundVersion = ""

                val versionsToTry = listOf("v1beta", "v1alpha")

                for (version in versionsToTry) {
                    try {
                        val request = okhttp3.Request.Builder()
                            .url("https://generativelanguage.googleapis.com/$version/models?key=$currentApiKey")
                            .build()
                        val response = httpClient.newCall(request).execute()
                        // FIX: guard against null/empty/non-JSON body that previously caused crash
                        val jsonStr = response.body?.string()?.takeIf { it.isNotBlank() } ?: continue
                        response.close()

                        val root = try {
                            org.json.JSONObject(jsonStr)
                        } catch (jsonEx: Exception) {
                            Log.w(TAG, "JSON parse failed for $version: ${jsonEx.message}")
                            continue
                        }

                        if (root.has("models")) {
                            val modelsArray = root.getJSONArray("models")
                            for (i in 0 until modelsArray.length()) {
                                val m = modelsArray.optJSONObject(i) ?: continue
                                if (m.has("supportedGenerationMethods")) {
                                    val methods = m.getJSONArray("supportedGenerationMethods")
                                    for (j in 0 until methods.length()) {
                                        if (methods.optString(j) == "bidiGenerateContent") {
                                            foundModel = m.optString("name", "")
                                            foundVersion = version
                                            break
                                        }
                                    }
                                }
                                if (foundModel.isNotEmpty()) break
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to query $version models: ${e.message}")
                    }
                    if (foundModel.isNotEmpty()) break
                }

                runOnUiThread {
                    if (foundModel.isNotEmpty()) {
                        statusText.text = "Connecting..."
                        Log.d(TAG, "Auto-discovered Live model: $foundModel on $foundVersion")
                        GeminiLiveClient.updateEndpointVersion(foundVersion)
                        connectWithModel(currentApiKey, fallbackApiKey, foundModel, prefs)
                    } else {
                        if (!isRetry && fallbackApiKey.isNotEmpty()) {
                            Log.w(TAG, "Primary key model fetch failed. Falling back to secondary.")
                            discoverModelAndConnect(fallbackApiKey, "", prefs, true)
                        } else {
                            statusText.text = "Error: No Live models found for this API key!"
                            showErrorDialog("Model Discovery Failed", "No Live models found for this API key!")
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    if (!isRetry && fallbackApiKey.isNotEmpty()) {
                        Log.w(TAG, "Primary key threw exception: ${e.message}. Falling back to secondary.")
                        discoverModelAndConnect(fallbackApiKey, "", prefs, true)
                    } else {
                        statusText.text = "Error finding model: ${e.message}"
                        showErrorDialog("Model Discovery Failed", e.message ?: "Unknown error")
                    }
                }
            }
        }
        thread.name = "GeminiModelDiscovery"
        thread.isDaemon = true
        thread.start()
    }

    private fun connectWithModel(apiKey: String, fallbackApiKey: String, model: String, prefs: SharedPreferences) {
        val voice     = prefs.getString("gemini_voice", "Aoede") ?: "Aoede"
        val userName  = prefs.getString("user_name", "Sir") ?: "Sir"
        val personality = prefs.getString("personality_mode", "gf") ?: "gf"

        val systemPrompt = buildSystemPrompt(userName, personality)

        geminiLive = GeminiLiveClient(apiKey, model, voice, systemPrompt)
        audioEngine = AudioEngine()

        // ── Wire AudioEngine callbacks ────────────────────────────────────────
        audioEngine?.onAudioChunkReady = { pcm ->
            geminiLive?.sendAudioChunk(pcm)
        }
        audioEngine?.onAmplitudeChanged = { rms ->
            runOnUiThread { waveformView.setAmplitude(rms) }
        }
        audioEngine?.onSpeakingStarted = {
            runOnUiThread {
                orbView.setState(OrbAnimationView.State.SPEAKING)
                statusText.text = "◈ SPEAKING"
                systemVitalsView?.setIsSpeaking(true)
                setActiveMode(true)
            }
        }
        audioEngine?.onSpeakingStopped = {
            runOnUiThread {
                orbView.setState(OrbAnimationView.State.LISTENING)
                statusText.text = "● LISTENING"
                systemVitalsView?.setIsSpeaking(false)
            }
        }

        // ── Wire GeminiLive callbacks ─────────────────────────────────────────
        geminiLive?.onConnected = {
            runOnUiThread {
                orbView.setState(OrbAnimationView.State.LISTENING)
                statusText.text = "● LISTENING"
                // FIX: Acquire WakeLock on connect to prevent CPU sleep killing audio session
                acquireWakeLock()
            }
            audioEngine?.startRecording()
            audioEngine?.startPlayback()

            if (isFirstConnection) {
                isFirstConnection = false
                // Greet user
                mainHandler.postDelayed({ sendGreeting(userName, personality) }, 600)
            }
        }

        geminiLive?.onAudioReceived = { pcm ->
            audioEngine?.queueAudio(pcm)
        }

        geminiLive?.onInputTranscript = { text ->
            inputBuffer.append(text)
            runOnUiThread {
                chatAdapter?.appendToLastMessage(text, isUser = true)
                chatAdapter?.let { adapter ->
                    chatRecycler?.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        geminiLive?.onOutputTranscript = { text ->
            outputBuffer.append(text)
            runOnUiThread {
                chatAdapter?.appendToLastMessage(text, isUser = false)
                chatAdapter?.let { adapter ->
                    chatRecycler?.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        geminiLive?.onTurnComplete = {
            val userText = inputBuffer.toString().trim()
            val aaradhyaText = outputBuffer.toString().trim()
            
            var didLiveSearch = false
            
            if (com.aaradhya.assistant.ai.LiveSearch.needsLiveSearch(userText)) {
                didLiveSearch = true
                // Interrupt any generic response Gemini might be giving
                geminiLive?.sendInterrupt()
                audioEngine?.interruptPlayback()
                
                runOnUiThread {
                    statusText.text = "Searching live internet... \ud83c\udf10"
                }
                
                CoroutineScope(Dispatchers.IO).launch {
                    val prefs = getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
                    val serpstackKey = prefs.getString("serpstack_api_key", "") ?: ""
                    val liveData = com.aaradhya.assistant.ai.LiveSearch.searchWeb(userText, serpstackKey)
                    val finalPrompt = """
                        User asked: $userText
                        
                        Live Internet Information:
                        $liveData
                        
                        Answer naturally in Hinglish based on the Live Internet Information provided.
                    """.trimIndent()
                    geminiLive?.sendText(finalPrompt)
                }
            } else if (!lastTurnHadFunctionCall) {
                // Try parsing user's words first
                var cmd = CommandParser.parse(userText)
                
                // If user text is empty or didn't match, parse the AI's hallucinated response (e.g. "camera khol dia")
                if (cmd == null && aaradhyaText.isNotEmpty()) {
                    cmd = CommandParser.parse(aaradhyaText)
                    if (cmd != null) {
                        Log.w(TAG, "Command parsed from AI hallucination/fallback: ${cmd.type}")
                    }
                }
                
                cmd?.let { viewModel.executeCommand(it) }
            }

            if (aaradhyaText.isNotEmpty() && !didLiveSearch) {
                // Already appended via onOutputTranscript
            }

            if (userText.isNotEmpty() || aaradhyaText.isNotEmpty()) {
                if (userText.isNotEmpty()) conversationHistory.add("User ($userName): $userText")
                if (aaradhyaText.isNotEmpty()) conversationHistory.add("AARADHYA: $aaradhyaText")
                
                while (conversationHistory.size > 20) {
                    conversationHistory.removeAt(0)
                }
                
                geminiLive?.systemPrompt = buildSystemPrompt(userName, personality)
            }

            inputBuffer.clear()
            outputBuffer.clear()
            lastTurnHadFunctionCall = false
        }

        geminiLive?.onError = { err ->
            runOnUiThread {
                Log.e(TAG, "Gemini error: $err")
                statusText.text = "Error: $err"
                orbView.setState(OrbAnimationView.State.IDLE)
                showErrorDialog("Aaradhya Connection Error", err)
            }
        }

        geminiLive?.onFatalError = { err ->
            runOnUiThread {
                Log.e(TAG, "Gemini fatal error: $err")
                if (fallbackApiKey.isNotEmpty()) {
                    Log.w(TAG, "Fatal websocket error on primary key. Swapping to secondary.")
                    geminiLive?.disconnect()
                    geminiLive = null
                    statusText.text = "Switching to Secondary Key..."
                    // Pass empty fallback to avoid infinite loops
                    discoverModelAndConnect(fallbackApiKey, "", prefs, true)
                } else {
                    statusText.text = "Fatal Error: $err"
                    orbView.setState(OrbAnimationView.State.IDLE)
                    // Clean up and release the AudioEngine to prevent process hangs
                    audioEngine?.release()
                    audioEngine = null
                    showErrorDialog("Aaradhya Connection Fatal Error", err)
                }
            }
        }

        geminiLive?.onDisconnected = { reason ->
            runOnUiThread {
                statusText.text = "Reconnect: $reason"
                orbView.setState(OrbAnimationView.State.THINKING)
                // FIX: Keep WakeLock during reconnect so coroutines stay alive
            }
        }

        geminiLive?.onFunctionCall = { name, args ->
            lastTurnHadFunctionCall = true
            Log.d(TAG, "Function call received: $name → $args")

            // Map Gemini function name → AppCommand, execute, then respond to Gemini
            val command: AppCommand? = when (name) {
                "open_app"          -> AppCommand(AppCommand.OPEN_APP,
                    mapOf("app" to args.optString("app_name", "")))

                "make_call"         -> AppCommand(AppCommand.MAKE_CALL,
                    mapOf("name"   to args.optString("name", ""),
                          "number" to args.optString("number", "")))

                "set_alarm"         -> AppCommand(AppCommand.SET_ALARM,
                    mapOf("hour"   to args.optInt("hour", -1).toString(),
                          "minute" to args.optInt("minute", 0).toString(),
                          "label"  to args.optString("label", "Aaradhya Alarm")))

                "set_timer"         -> AppCommand(AppCommand.SET_TIMER,
                    mapOf("seconds" to args.optInt("seconds", 60).toString(),
                          "label"   to args.optString("label", "Aaradhya Timer")))

                "navigate_to"       -> AppCommand(AppCommand.NAVIGATE_TO,
                    mapOf("destination" to args.optString("destination", "")))

                "play_music"        -> AppCommand(AppCommand.PLAY_MUSIC,
                    mapOf("query" to args.optString("query", "")))

                "play_youtube"      -> AppCommand(AppCommand.PLAY_YOUTUBE,
                    mapOf("query" to args.optString("query", "")))

                "search_web"        -> AppCommand(AppCommand.SEARCH_WEB,
                    mapOf("query" to args.optString("query", "")))

                "send_whatsapp"     -> AppCommand(AppCommand.WHATSAPP_MSG,
                    mapOf("name" to args.optString("name", "")))

                "toggle_flashlight" -> {
                    val on = args.optBoolean("on", true)
                    AppCommand(if (on) AppCommand.FLASHLIGHT_ON else AppCommand.FLASHLIGHT_OFF)
                }

                "set_volume"        -> {
                    val dir = args.optString("direction", "up").lowercase()
                    AppCommand(if (dir == "up") AppCommand.VOLUME_UP else AppCommand.VOLUME_DOWN)
                }

                "open_settings"     -> when (args.optString("type", "general").lowercase()) {
                    "wifi"      -> AppCommand(AppCommand.OPEN_WIFI_SETTINGS)
                    "bluetooth" -> AppCommand(AppCommand.OPEN_BT_SETTINGS)
                    "battery"   -> AppCommand(AppCommand.OPEN_BATTERY_SETTINGS)
                    else        -> AppCommand(AppCommand.OPEN_SETTINGS)
                }

                "lock_screen"       -> AppCommand(AppCommand.LOCK_SCREEN)
                "go_home"           -> AppCommand(AppCommand.GO_HOME)
                "go_back"           -> AppCommand(AppCommand.GO_BACK)

                else -> null
            }

            if (command != null) {
                viewModel.executeCommand(command) { result ->
                    // Send the result back to Gemini so it can speak naturally
                    val response = org.json.JSONObject().apply {
                        put("output", result)
                        put("status", "success")
                    }
                    geminiLive?.sendFunctionResponse(name, response)
                    Log.d(TAG, "Function $name result → $result")
                }
            } else {
                // Unknown function — tell Gemini
                val response = org.json.JSONObject().apply {
                    put("output", "Function '$name' not implemented.")
                    put("status", "error")
                }
                geminiLive?.sendFunctionResponse(name, response)
            }
        }

        geminiLive?.connect()
    }

    // ── WakeLock Helpers ──────────────────────────────────────────────────────

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        try {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AARADHYA::AudioSessionWakeLock"
            ).apply {
                // Auto-release after 30 minutes as a safety net
                acquire(30 * 60 * 1000L)
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "WakeLock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock: ${e.message}")
        } finally {
            wakeLock = null
        }
    }

    private fun sendGreeting(userName: String, personality: String) {
        val greeting = when (personality) {
            "professional" -> "Good day $userName. AARADHYA is online and ready to assist you."
            "assistant"    -> "Hello $userName! Main AARADHYA hoon. Kaise help karun aapki?"
            else           -> "Hey $userName! Main aa gayi hoon. Kya help chahiye tumhe?"
        }
        geminiLive?.sendText(greeting)
    }

    private fun buildSystemPrompt(userName: String, personality: String): String {
        val now = java.text.SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.ENGLISH).format(Date())
        val personalityBlock = when (personality) {
            "professional" -> """
                You are AARADHYA, a professional AI assistant.
                - Speak formal English only
                - Be precise and efficient
                - No emojis
                - Max 2 sentences per response
            """.trimIndent()
            "assistant" -> """
                You are AARADHYA, a friendly AI assistant.
                - Use friendly Hinglish or English
                - Be balanced and helpful
                - Max 2-3 sentences per response
            """.trimIndent()
            else -> """
                You are AARADHYA, the user's AI companion girlfriend.
                - Name: AARADHYA
                - Language: Hinglish (Hindi + English mix) — spoken naturally
                - Tone: Warm, caring, emotionally expressive
                - Use: "tumhara", "haan", "acha", "bilkul"
                - Expressions: "main yahan hoon", "tumne yaad kiya?"
                - Max 2-3 sentences per response
                - Sound natural when speaking aloud
                - Examples: "Haan! Abhi kar deti hoon", "Bilkul! Tumhara kaam ho gaya"
            """.trimIndent()
        }

        val historyContext = if (conversationHistory.isNotEmpty()) {
            "\nPast Conversation History:\n" + conversationHistory.joinToString("\n")
        } else ""

        val installedApps = com.aaradhya.assistant.apps.InstalledAppsManager.getAllApps(this)
            .joinToString(", ") { it.label }

        return """
            Current date/time: $now
            User's name: $userName

            $personalityBlock
            $historyContext

            ══════════════════════════════════════════════
            FUNCTION CALLING — CRITICAL INSTRUCTIONS
            ══════════════════════════════════════════════
            You have access to FUNCTION TOOLS to control the phone. You MUST use these
            function calls whenever the user asks you to perform a phone action.
            DO NOT just describe what you're doing — CALL THE FUNCTION.

            Available functions:
            • open_app(app_name)           — Open any installed app
            • make_call(name, number)      — Call a contact or number
            • set_alarm(hour, minute, label) — Set an alarm (24-hour format)
            • set_timer(seconds, label)    — Set a countdown timer
            • navigate_to(destination)     — Google Maps navigation
            • play_music(query)            — Open music/play a song
            • play_youtube(query)          — Search & play on YouTube
            • search_web(query)            — Google search in browser
            • send_whatsapp(name)          — Open WhatsApp chat
            • toggle_flashlight(on)        — Torch on/off
            • set_volume(direction)        — Volume up/down
            • open_settings(type)          — Open settings (general/wifi/bluetooth/battery)
            • lock_screen()                — Lock the phone screen immediately
            • go_home()                    — Go to the home screen
            • go_back()                    — Go back to the previous screen


            WORKFLOW:
            1. User asks to do something → you CALL the matching function
            2. You get back a result → you confirm naturally in speech
            3. Example: User says "call mom" → you call make_call(name="mom") → 
               then say "Mom ko call kar raha hoon! 📞"

            CURRENTLY INSTALLED APPS ON THE PHONE:
            $installedApps

            When the user asks to open an app:
            - Check the installed apps list (fuzzy match is fine)
            - If installed: call open_app() and confirm cheerfully
            - If NOT installed: say it's not installed, don't pretend to open it

            ══════════════════════════════════════════════
            IMPORTANT: You are speaking ALOUD — keep responses short and natural.
            Never use markdown formatting. Speak like you're talking, not writing.
        """.trimIndent()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Mic & Interrupt
    // ══════════════════════════════════════════════════════════════════════════

    private fun toggleMute() {
        isMuted = !isMuted
        audioEngine?.isMuted = isMuted
        micButton.setImageResource(if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on)
        statusText.text = if (isMuted) "⊘ MUTED" else "● LISTENING"
        orbView.setState(if (isMuted) OrbAnimationView.State.MUTED else OrbAnimationView.State.LISTENING)
    }

    private fun interruptAaradhya() {
        geminiLive?.sendInterrupt()
        audioEngine?.interruptPlayback()
        statusText.text = "● LISTENING"
        Toast.makeText(this, "AARADHYA ko roka gaya!", Toast.LENGTH_SHORT).show()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Active Mode (red overlay)
    // ══════════════════════════════════════════════════════════════════════════

    private fun setActiveMode(active: Boolean) {
        val toAlpha = if (active) 0.08f else 0f
        val duration = if (active) 300L else 500L
        ObjectAnimator.ofFloat(redOverlay, "alpha", redOverlay.alpha, toAlpha)
            .apply { this.duration = duration }
            .start()
        if (!active) {
            orbView.setState(OrbAnimationView.State.IDLE)
            waveformView.stopAnimation()
        } else {
            waveformView.startAnimation()
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Status Bar Updates
    // ══════════════════════════════════════════════════════════════════════════

    private fun updateStatusBar() {
        // Battery
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        batteryText.text = "🔋 ${if (batteryPct > 0) "$batteryPct%" else "--"}"

        // RAM
        val am = getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mi)
        val ramFreeMb = mi.availMem / (1024 * 1024)
        val ramTotalMb = mi.totalMem / (1024 * 1024)
        val ramUsagePct = if (ramTotalMb > 0) ((ramTotalMb - ramFreeMb) * 100 / ramTotalMb).toInt() else 0
        
        ramText?.text = "RAM: ${ramFreeMb}MB"

        // Time
        timeText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        // System Vitals View (Landscape only)
        systemVitalsView?.let { vitals ->
            val netTx = android.net.TrafficStats.getTotalTxBytes() / 1_048_576f
            val netRx = android.net.TrafficStats.getTotalRxBytes() / 1_048_576f
            
            val uptimeMillis = android.os.SystemClock.elapsedRealtime()
            val h = (uptimeMillis / 3600000)
            val m = (uptimeMillis % 3600000) / 60000
            val uptimeStr = String.format("%02dh %02dm", h, m)
            
            val procs = am?.runningAppProcesses?.size ?: 0
            
            // UI mock for CPU
            val cpuLoad = kotlin.random.Random.nextInt(15, 60)
            
            vitals.updateVitals(cpuLoad, ramUsagePct, batteryPct, netTx, netRx, uptimeStr, procs)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Bulletproof Crash & Error Handling
    // ══════════════════════════════════════════════════════════════════════════

    private fun setupCrashProtector() {
        // 1. Intercept background thread uncaught exceptions to prevent silent force-closes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught Exception on background thread: ${thread.name}", throwable)
            runOnUiThread {
                showCrashDialog(throwable)
            }
        }

        // 2. Intercept main (UI) thread exceptions. Loop the UI thread dynamically
        // so UI crashes never terminate the app.
        mainHandler.post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    Log.e(TAG, "Caught exception on UI thread Looper", e)
                    showCrashDialog(e)
                }
            }
        }
    }

    @Volatile
    private var isShowingCrash = false

    private fun showCrashDialog(throwable: Throwable) {
        if (isShowingCrash) {
            Log.e(TAG, "Prevented recursive crash loop from: ${throwable.message}")
            return
        }
        isShowingCrash = true
        val errorMsg = throwable.localizedMessage ?: throwable.toString()
        val stackTrace = Log.getStackTraceString(throwable)
        showErrorDialog("App Crash Exception", "$errorMsg\n\n$stackTrace") {
            isShowingCrash = false
        }
    }

    private fun showErrorDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        runOnUiThread {
            // Append error to chat adapter safely
            try {
                chatAdapter?.addMessage(ChatMessage("⚠️ $title: $message", isUser = false))
                chatAdapter?.let { adapter ->
                    chatRecycler?.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating chat list: ${e.message}")
            }

            try {
                androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("⚠️ $title")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Retry Connection") { dialog, _ ->
                        dialog.dismiss()
                        onDismiss?.invoke()
                        initGeminiLive()
                    }
                    .setNegativeButton("Close Dialog") { dialog, _ ->
                        dialog.dismiss()
                        onDismiss?.invoke()
                    }
                    .show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build or present error dialog: ${e.message}")
                onDismiss?.invoke()
            }
        }
    }
}
