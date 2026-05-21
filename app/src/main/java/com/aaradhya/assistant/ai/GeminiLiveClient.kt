package com.aaradhya.assistant.ai

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Manages the WebSocket connection to Google Gemini Live BidiGenerateContent API.
 * Handles setup, audio streaming, text messages, keepalive, and session renewal.
 */
class GeminiLiveClient(
    private val apiKey: String,
    private val model: String,
    private val voiceName: String,
    var systemPrompt: String
) {
    companion object {
        private const val TAG = "GeminiLiveClient"
        var WS_BASE = "wss://generativelanguage.googleapis.com/ws/" +
                "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
        
        fun updateEndpointVersion(version: String) {
            WS_BASE = "wss://generativelanguage.googleapis.com/ws/" +
                    "google.ai.generativelanguage.$version.GenerativeService.BidiGenerateContent"
        }
        private const val SESSION_RENEW_AFTER = 480_000L  // 8 minutes in ms (reduced to avoid API timeout limits)
        private const val RECONNECT_DELAY     =   3_000L  // 3 seconds
    }

    // ── Callbacks ──────────────────────────────────────────────────────────────
    var onConnected: (() -> Unit)? = null
    var onAudioReceived: ((ByteArray) -> Unit)? = null
    var onInputTranscript: ((String) -> Unit)? = null
    var onOutputTranscript: ((String) -> Unit)? = null
    var onTurnComplete: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onFatalError: ((String) -> Unit)? = null
    var onDisconnected: ((String) -> Unit)? = null
    var onFunctionCall: ((String, JSONObject) -> Unit)? = null

    // ── State ──────────────────────────────────────────────────────────────────
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var shouldReconnect = true
    private var sessionStartTime = 0L

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "GeminiLiveClient Coroutine Exception: ${exception.message}")
        onError?.invoke(exception.localizedMessage ?: exception.toString())
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private var renewalJob: Job? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)   // No read timeout for streaming
        .writeTimeout(0, TimeUnit.MILLISECONDS)  // No write timeout for streaming
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    // ── Public API ─────────────────────────────────────────────────────────────

    fun connect() {
        shouldReconnect = true
        // FIX: Reset hasFatalError so reconnects work after a previous fatal error
        hasFatalError = false
        openWebSocket()
    }

    fun disconnect() {
        shouldReconnect = false
        renewalJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
    }

    /** Send raw PCM-16 bytes (16kHz) to Gemini as realtime audio input */
    fun sendAudioChunk(pcmBytes: ByteArray) {
        if (!isConnected) return
        val b64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
        val msg = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("mediaChunks", JSONArray().apply {
                    put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", b64)
                    })
                })
            })
        }
        sendJson(msg)
    }

    /** Send a text message to Gemini */
    fun sendText(text: String) {
        if (!isConnected) return
        val msg = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                })
                put("turnComplete", true)
            })
        }
        sendJson(msg)
    }

    /** Interrupt AARADHYA mid-speech — stops server from sending more audio */
    fun sendInterrupt() {
        if (!isConnected) return
        val msg = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray())
                put("turnComplete", true)
            })
        }
        sendJson(msg)
    }

    /** Send a function response back to Gemini */
    fun sendFunctionResponse(name: String, responseData: JSONObject) {
        if (!isConnected) return
        val msg = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("functionResponse", JSONObject().apply {
                                    put("name", name)
                                    put("response", responseData)
                                })
                            })
                        })
                    })
                })
                put("turnComplete", true)
            })
        }
        sendJson(msg)
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private fun openWebSocket() {
        val url = "$WS_BASE?key=$apiKey"
        Log.d(TAG, "Opening WebSocket: $WS_BASE (key hidden)")
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    private fun sendJson(json: JSONObject) {
        if (!isConnected) return   // FIX: guard — don't send on a disconnected socket
        try {
            val ws = webSocket ?: return
            
            // FIX: Prevent OOM on slow networks. OkHttp's queue is unbounded.
            // If upload speed is slower than audio recording speed, queue grows infinitely.
            if (ws.queueSize() > 1_000_000L) { // 1 MB limit
                Log.w(TAG, "WebSocket queue too large (${ws.queueSize()} bytes). Dropping frame to prevent OOM crash.")
                return
            }
            
            ws.send(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "sendJson error: ${e.message}")
        }
    }

    private fun sendSetupMessage() {
        val setup = JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", model)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", voiceName)
                            })
                        })
                    })
                    put("temperature", 0.9)
                })
                // ── Function Tool Declarations ─────────────────────────────────
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("functionDeclarations", JSONArray().apply {

                            // open_app
                            put(buildFn("open_app",
                                "Open any installed Android application by name",
                                mapOf("app_name" to ("string" to "The name of the app to open, e.g. WhatsApp, YouTube, Instagram"))
                            ))

                            // make_call
                            put(buildFn("make_call",
                                "Make a phone call to a contact or number",
                                mapOf(
                                    "name"   to ("string" to "Contact name to call"),
                                    "number" to ("string" to "Phone number to dial directly (optional)")
                                )
                            ))

                            // set_alarm
                            put(buildFn("set_alarm",
                                "Set an alarm at a specific time",
                                mapOf(
                                    "hour"   to ("integer" to "Hour in 24-hour format (0-23)"),
                                    "minute" to ("integer" to "Minute (0-59)"),
                                    "label"  to ("string"  to "Alarm label or title")
                                )
                            ))

                            // set_timer
                            put(buildFn("set_timer",
                                "Set a countdown timer for a specified duration",
                                mapOf(
                                    "seconds" to ("integer" to "Duration of the timer in total seconds"),
                                    "label"   to ("string"  to "Timer label")
                                )
                            ))

                            // navigate_to
                            put(buildFn("navigate_to",
                                "Open Google Maps navigation to a destination",
                                mapOf("destination" to ("string" to "The place or address to navigate to"))
                            ))

                            // play_music
                            put(buildFn("play_music",
                                "Open a music app or play music by name/artist/genre",
                                mapOf("query" to ("string" to "Optional song, artist, or genre name"))
                            ))

                            // play_youtube
                            put(buildFn("play_youtube",
                                "Search and play a video on YouTube",
                                mapOf("query" to ("string" to "Search query or video title to play on YouTube"))
                            ))

                            // search_web
                            put(buildFn("search_web",
                                "Search the web using Google in the browser",
                                mapOf("query" to ("string" to "The search query"))
                            ))

                            // send_whatsapp
                            put(buildFn("send_whatsapp",
                                "Open WhatsApp chat with a contact",
                                mapOf("name" to ("string" to "Contact name to open WhatsApp with"))
                            ))

                            // toggle_flashlight
                            put(buildFn("toggle_flashlight",
                                "Turn the phone flashlight (torch) on or off",
                                mapOf("on" to ("boolean" to "true to turn on, false to turn off"))
                            ))

                            // set_volume
                            put(buildFn("set_volume",
                                "Adjust the media volume up or down",
                                mapOf("direction" to ("string" to "Either 'up' or 'down'"))
                            ))

                            // open_settings
                            put(buildFn("open_settings",
                                "Open a system settings screen",
                                mapOf("type" to ("string" to "One of: general, wifi, bluetooth, battery"))
                            ))

                            // lock_screen
                            put(buildFn("lock_screen",
                                "Lock the phone screen immediately",
                                emptyMap()
                            ))

                            // go_home
                            put(buildFn("go_home",
                                "Navigate to the phone home screen",
                                emptyMap()
                            ))

                            // go_back
                            put(buildFn("go_back",
                                "Perform a back navigation action (simulate pressing back button)",
                                emptyMap()
                            ))
                        })
                    })
                })
            })
        }
        sendJson(setup)
    }

    /** Helper to build a Gemini function declaration JSON object */
    private fun buildFn(
        name: String,
        description: String,
        params: Map<String, Pair<String, String>>   // paramName → (type, description)
    ): JSONObject = JSONObject().apply {
        put("name", name)
        put("description", description)
        put("parameters", JSONObject().apply {
            put("type", "object")
            val props = JSONObject()
            params.forEach { (paramName, typeAndDesc) ->
                val (type, desc) = typeAndDesc
                props.put(paramName, JSONObject().apply {
                    put("type", type)
                    put("description", desc)
                })
            }
            put("properties", props)
        })
    }

    private fun startSessionRenewal() {
        renewalJob?.cancel()
        renewalJob = scope.launch {
            delay(SESSION_RENEW_AFTER)
            if (isActive && isConnected) {
                Log.d(TAG, "Session renewal triggered after 8 minutes")
                val oldSocket = webSocket
                webSocket = null
                isConnected = false
                // FIX: Reset hasFatalError so the renewed session can reconnect
                hasFatalError = false
                try { oldSocket?.close(1000, "Session renewal") } catch (e: Exception) {
                    Log.w(TAG, "Error closing socket during renewal: ${e.message}")
                }
                delay(500)
                if (shouldReconnect) openWebSocket()
            }
        }
    }

    private fun handleReconnect() {
        scope.launch {
            delay(RECONNECT_DELAY)
            if (shouldReconnect) {
                Log.d(TAG, "Attempting reconnect...")
                openWebSocket()
            }
        }
    }

    private var hasFatalError = false

    private fun parseMessage(text: String) {
        try {
            val root = if (text.trimStart().startsWith("[")) {
                JSONArray(text).optJSONObject(0) ?: JSONObject()
            } else {
                JSONObject(text)
            }

            // ── Error handling ─────────────────────────────────────────────────
            if (root.has("error")) {
                val errorObj = root.optJSONObject("error")
                val errMsg = errorObj?.optString("message") ?: "Unknown API Error"
                Log.e(TAG, "API Error: $errMsg")
                
                // Handle known Gemini session timeouts/unsupported states gracefully
                if (errMsg.contains("Operation is not implemented") || 
                    errMsg.contains("supported") || 
                    errMsg.contains("enabled")) {
                    Log.d(TAG, "Treating known API error as session timeout, reconnecting...")
                    val oldSocket = webSocket
                    webSocket = null
                    isConnected = false
                    oldSocket?.close(1000, "Session recovery")
                    
                    if (shouldReconnect) handleReconnect()
                    return
                }

                hasFatalError = true
                shouldReconnect = false
                onFatalError?.invoke(errMsg)
                return
            }

            // ── Setup complete ─────────────────────────────────────────────────
            if (root.has("setupComplete")) {
                Log.d(TAG, "Setup complete received")
                onConnected?.invoke()
                startSessionRenewal()
                return
            }

            // ── Server content ─────────────────────────────────────────────────
            val sc = root.optJSONObject("serverContent") ?: return

            // Audio from model
            sc.optJSONObject("modelTurn")?.optJSONArray("parts")?.let { parts ->
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i) ?: continue
                    
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val b64 = inlineData.optString("data", "") 
                        if (b64.isNotEmpty()) {
                            val pcm = Base64.decode(b64, Base64.DEFAULT)
                            onAudioReceived?.invoke(pcm)
                        }
                    }
                    
                    val functionCall = part.optJSONObject("functionCall")
                    if (functionCall != null) {
                        val name = functionCall.optString("name")
                        val args = functionCall.optJSONObject("args")
                        if (args != null) {
                            onFunctionCall?.invoke(name, args)
                        }
                    }
                }
            }

            // Output transcription (what AARADHYA said)
            sc.optJSONObject("outputTranscription")?.optString("text")?.takeIf { it.isNotEmpty() }?.let {
                onOutputTranscript?.invoke(it)
            }

            // Input transcription (what user said)
            sc.optJSONObject("inputTranscription")?.optString("text")?.takeIf { it.isNotEmpty() }?.let {
                onInputTranscript?.invoke(it)
            }

            // Turn complete
            if (sc.optBoolean("turnComplete", false)) {
                onTurnComplete?.invoke()
            }

        } catch (e: Exception) {
            Log.e(TAG, "parseMessage error: ${e.message}")
        }
    }

    // ── WebSocket Listener ─────────────────────────────────────────────────────

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            isConnected = true
            sessionStartTime = System.currentTimeMillis()
            sendSetupMessage()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            parseMessage(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
            parseMessage(bytes.utf8())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
            isConnected = false
            renewalJob?.cancel()
            onError?.invoke(t.message ?: "Unknown error")
            if (shouldReconnect) handleReconnect()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code $reason")
            isConnected = false
            renewalJob?.cancel()

            // 1007 = invalid payload, 1011 = server internal error (unrecoverable)
            // 1008 = policy violation — only unrecoverable if it's NOT a known session-timeout message
            val isRecoverable1008 = code == 1008 &&
                (reason.contains("Operation is not implemented") || reason.contains("supported"))
            val isUnrecoverable = code == 1007 || code == 1011 || (code == 1008 && !isRecoverable1008)

            if (!hasFatalError) {
                val disconnectReason = if (reason.isNotEmpty()) reason else "Code: $code"
                onDisconnected?.invoke(disconnectReason)

                when {
                    // FIX: code 1000 (normal close) from session renewal OR server timeout → reconnect silently
                    shouldReconnect && code == 1000 -> {
                        Log.d(TAG, "Clean close (code 1000) — reconnecting. Reason: $reason")
                        handleReconnect()
                    }
                    shouldReconnect && code != 1000 && !isUnrecoverable -> {
                        handleReconnect()
                    }
                    isUnrecoverable -> {
                        Log.e(TAG, "Unrecoverable close code $code: $reason")
                        hasFatalError = true
                        shouldReconnect = false
                        onFatalError?.invoke(disconnectReason)
                    }
                }
            }
        }
    }
}
