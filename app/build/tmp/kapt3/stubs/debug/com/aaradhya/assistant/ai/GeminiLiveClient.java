package com.aaradhya.assistant.ai;

/**
 * Manages the WebSocket connection to Google Gemini Live BidiGenerateContent API.
 * Handles setup, audio streaming, text messages, keepalive, and session renewal.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\b\u0012\u0018\u0000 [2\u00020\u0001:\u0001[B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J8\u0010E\u001a\u00020(2\u0006\u0010F\u001a\u00020\u00032\u0006\u0010G\u001a\u00020\u00032\u001e\u0010H\u001a\u001a\u0012\u0004\u0012\u00020\u0003\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030J0IH\u0002J\u0006\u0010K\u001a\u00020\u0012J\u0006\u0010L\u001a\u00020\u0012J\b\u0010M\u001a\u00020\u0012H\u0002J\b\u0010N\u001a\u00020\u0012H\u0002J\u0010\u0010O\u001a\u00020\u00122\u0006\u0010P\u001a\u00020\u0003H\u0002J\u000e\u0010Q\u001a\u00020\u00122\u0006\u0010R\u001a\u00020\u0011J\u0016\u0010S\u001a\u00020\u00122\u0006\u0010F\u001a\u00020\u00032\u0006\u0010T\u001a\u00020(J\u0006\u0010U\u001a\u00020\u0012J\u0010\u0010V\u001a\u00020\u00122\u0006\u0010W\u001a\u00020(H\u0002J\b\u0010X\u001a\u00020\u0012H\u0002J\u000e\u0010Y\u001a\u00020\u00122\u0006\u0010P\u001a\u00020\u0003J\b\u0010Z\u001a\u00020\u0012H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R(\u0010\u000f\u001a\u0010\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\"\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0018X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR(\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u0014\"\u0004\b\u001f\u0010\u0016R(\u0010 \u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0014\"\u0004\b\"\u0010\u0016R(\u0010#\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010\u0014\"\u0004\b%\u0010\u0016R.\u0010&\u001a\u0016\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u0012\u0018\u00010\'X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010*\"\u0004\b+\u0010,R(\u0010-\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b.\u0010\u0014\"\u0004\b/\u0010\u0016R(\u00100\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b1\u0010\u0014\"\u0004\b2\u0010\u0016R\"\u00103\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0018X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b4\u0010\u001a\"\u0004\b5\u0010\u001cR\u0010\u00106\u001a\u0004\u0018\u000107X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00108\u001a\u000209X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010:\u001a\u00020;X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b=\u0010>\"\u0004\b?\u0010@R\u000e\u0010\u0005\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010A\u001a\u0004\u0018\u00010BX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010C\u001a\u00020DX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\\"}, d2 = {"Lcom/aaradhya/assistant/ai/GeminiLiveClient;", "", "apiKey", "", "model", "voiceName", "systemPrompt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "client", "Lokhttp3/OkHttpClient;", "exceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "hasFatalError", "", "isConnected", "onAudioReceived", "Lkotlin/Function1;", "", "", "getOnAudioReceived", "()Lkotlin/jvm/functions/Function1;", "setOnAudioReceived", "(Lkotlin/jvm/functions/Function1;)V", "onConnected", "Lkotlin/Function0;", "getOnConnected", "()Lkotlin/jvm/functions/Function0;", "setOnConnected", "(Lkotlin/jvm/functions/Function0;)V", "onDisconnected", "getOnDisconnected", "setOnDisconnected", "onError", "getOnError", "setOnError", "onFatalError", "getOnFatalError", "setOnFatalError", "onFunctionCall", "Lkotlin/Function2;", "Lorg/json/JSONObject;", "getOnFunctionCall", "()Lkotlin/jvm/functions/Function2;", "setOnFunctionCall", "(Lkotlin/jvm/functions/Function2;)V", "onInputTranscript", "getOnInputTranscript", "setOnInputTranscript", "onOutputTranscript", "getOnOutputTranscript", "setOnOutputTranscript", "onTurnComplete", "getOnTurnComplete", "setOnTurnComplete", "renewalJob", "Lkotlinx/coroutines/Job;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "sessionStartTime", "", "shouldReconnect", "getSystemPrompt", "()Ljava/lang/String;", "setSystemPrompt", "(Ljava/lang/String;)V", "webSocket", "Lokhttp3/WebSocket;", "webSocketListener", "Lokhttp3/WebSocketListener;", "buildFn", "name", "description", "params", "", "Lkotlin/Pair;", "connect", "disconnect", "handleReconnect", "openWebSocket", "parseMessage", "text", "sendAudioChunk", "pcmBytes", "sendFunctionResponse", "responseData", "sendInterrupt", "sendJson", "json", "sendSetupMessage", "sendText", "startSessionRenewal", "Companion", "app_debug"})
public final class GeminiLiveClient {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String apiKey = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String model = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String voiceName = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String systemPrompt;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "GeminiLiveClient";
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String WS_BASE = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent";
    private static final long SESSION_RENEW_AFTER = 480000L;
    private static final long RECONNECT_DELAY = 3000L;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onConnected;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> onAudioReceived;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onInputTranscript;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onOutputTranscript;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onTurnComplete;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFatalError;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDisconnected;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function2<? super java.lang.String, ? super org.json.JSONObject, kotlin.Unit> onFunctionCall;
    @org.jetbrains.annotations.Nullable()
    private okhttp3.WebSocket webSocket;
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private long sessionStartTime = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineExceptionHandler exceptionHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job renewalJob;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    private boolean hasFatalError = false;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.WebSocketListener webSocketListener = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.ai.GeminiLiveClient.Companion Companion = null;
    
    public GeminiLiveClient(@org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    java.lang.String model, @org.jetbrains.annotations.NotNull()
    java.lang.String voiceName, @org.jetbrains.annotations.NotNull()
    java.lang.String systemPrompt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSystemPrompt() {
        return null;
    }
    
    public final void setSystemPrompt(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnConnected() {
        return null;
    }
    
    public final void setOnConnected(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<byte[], kotlin.Unit> getOnAudioReceived() {
        return null;
    }
    
    public final void setOnAudioReceived(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnInputTranscript() {
        return null;
    }
    
    public final void setOnInputTranscript(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnOutputTranscript() {
        return null;
    }
    
    public final void setOnOutputTranscript(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnTurnComplete() {
        return null;
    }
    
    public final void setOnTurnComplete(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnError() {
        return null;
    }
    
    public final void setOnError(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnFatalError() {
        return null;
    }
    
    public final void setOnFatalError(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnDisconnected() {
        return null;
    }
    
    public final void setOnDisconnected(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function2<java.lang.String, org.json.JSONObject, kotlin.Unit> getOnFunctionCall() {
        return null;
    }
    
    public final void setOnFunctionCall(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super org.json.JSONObject, kotlin.Unit> p0) {
    }
    
    public final void connect() {
    }
    
    public final void disconnect() {
    }
    
    /**
     * Send raw PCM-16 bytes (16kHz) to Gemini as realtime audio input
     */
    public final void sendAudioChunk(@org.jetbrains.annotations.NotNull()
    byte[] pcmBytes) {
    }
    
    /**
     * Send a text message to Gemini
     */
    public final void sendText(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    /**
     * Interrupt AARADHYA mid-speech — stops server from sending more audio
     */
    public final void sendInterrupt() {
    }
    
    /**
     * Send a function response back to Gemini
     */
    public final void sendFunctionResponse(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    org.json.JSONObject responseData) {
    }
    
    private final void openWebSocket() {
    }
    
    private final void sendJson(org.json.JSONObject json) {
    }
    
    private final void sendSetupMessage() {
    }
    
    /**
     * Helper to build a Gemini function declaration JSON object
     */
    private final org.json.JSONObject buildFn(java.lang.String name, java.lang.String description, java.util.Map<java.lang.String, kotlin.Pair<java.lang.String, java.lang.String>> params) {
        return null;
    }
    
    private final void startSessionRenewal() {
    }
    
    private final void handleReconnect() {
    }
    
    private final void parseMessage(java.lang.String text) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\f\u00a8\u0006\u0010"}, d2 = {"Lcom/aaradhya/assistant/ai/GeminiLiveClient$Companion;", "", "()V", "RECONNECT_DELAY", "", "SESSION_RENEW_AFTER", "TAG", "", "WS_BASE", "getWS_BASE", "()Ljava/lang/String;", "setWS_BASE", "(Ljava/lang/String;)V", "updateEndpointVersion", "", "version", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWS_BASE() {
            return null;
        }
        
        public final void setWS_BASE(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        public final void updateEndpointVersion(@org.jetbrains.annotations.NotNull()
        java.lang.String version) {
        }
    }
}