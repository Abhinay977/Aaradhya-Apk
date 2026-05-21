package com.aaradhya.assistant.viewmodel;

/**
 * Central command executor for AARADHYA.
 *
 * Hybrid architecture:
 * 1. Normal Android Intent (primary)
 * 2. AccessibilityHelperService (fallback + UI interactions)
 *
 * Exposes [commandResult] LiveData — MainActivity forwards this back to Gemini as speech.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u001e\n\u0002\u0010\b\n\u0002\b\u0015\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bH\u0002J\u0010\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u0010\u0010\u0019\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bH\u0002J\u0010\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u0012\u0010\u001b\u001a\u00020\u000b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u000bH\u0002J&\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u001e\u001a\u00020\u001f2\u0016\b\u0002\u0010 \u001a\u0010\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000f\u0018\u00010\u000eJ\u0016\u0010!\u001a\u00020\u00182\u0006\u0010\"\u001a\u00020\u0007H\u0082@\u00a2\u0006\u0002\u0010#J\u0018\u0010$\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0&0%J\b\u0010\'\u001a\u00020\u000bH\u0002J\b\u0010(\u001a\u00020\u000bH\u0002J\u0006\u0010)\u001a\u00020\u0018J\u0016\u0010*\u001a\u00020\u000b2\u0006\u0010+\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\b\u0010-\u001a\u00020\u000bH\u0002J\u0010\u0010.\u001a\u0004\u0018\u00010\u000b2\u0006\u0010/\u001a\u00020\u000bJ\u001e\u00100\u001a\u00020\u000b2\u0006\u0010/\u001a\u00020\u000b2\u0006\u00101\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00102J\b\u00103\u001a\u00020\u000bH\u0002J\u0016\u00104\u001a\u00020\u000b2\u0006\u00105\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u000e\u00106\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00107J\u000e\u00108\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00107J\u0016\u00109\u001a\u00020\u000b2\u0006\u0010:\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\b\u0010;\u001a\u00020\u000bH\u0002J\u000e\u0010<\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00107J\u0016\u0010=\u001a\u00020\u000b2\u0006\u0010/\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u000e\u0010>\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00107J\u0016\u0010?\u001a\u00020\u000b2\u0006\u0010@\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u0016\u0010A\u001a\u00020\u000b2\u0006\u0010@\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u0016\u0010B\u001a\u00020\u000b2\u0006\u0010@\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u0016\u0010C\u001a\u00020\u000b2\u0006\u0010D\u001a\u00020EH\u0082@\u00a2\u0006\u0002\u0010FJ \u0010G\u001a\u00020\u000f2\u0018\u0010H\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0&0%J\u0016\u0010I\u001a\u00020\u000b2\u0006\u0010@\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010,J\u001e\u0010J\u001a\u00020\u000b2\u0006\u0010/\u001a\u00020\u000b2\u0006\u0010K\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u00102J&\u0010L\u001a\u00020\u000b2\u0006\u0010M\u001a\u00020E2\u0006\u0010N\u001a\u00020E2\u0006\u0010O\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010PJ\u001e\u0010Q\u001a\u00020\u000b2\u0006\u0010R\u001a\u00020E2\u0006\u0010O\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010SJ\b\u0010T\u001a\u00020\u000bH\u0002J\u0016\u0010U\u001a\u00020\u000b2\u0006\u0010V\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u0010WJ\u0010\u0010X\u001a\u00020\u000b2\u0006\u0010V\u001a\u00020\u0018H\u0002J\u0016\u0010Y\u001a\u00020\u000b2\u0006\u0010V\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u0010WR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0019\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\tR(\u0010\r\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u000f\u0018\u00010\u000eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013\u00a8\u0006Z"}, d2 = {"Lcom/aaradhya/assistant/viewmodel/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "actionIntent", "Landroidx/lifecycle/MutableLiveData;", "Landroid/content/Intent;", "getActionIntent", "()Landroidx/lifecycle/MutableLiveData;", "commandResult", "", "getCommandResult", "intentExecutor", "Lkotlin/Function1;", "", "getIntentExecutor", "()Lkotlin/jvm/functions/Function1;", "setIntentExecutor", "(Lkotlin/jvm/functions/Function1;)V", "accessibilityClickText", "text", "accessibilityScroll", "up", "", "accessibilityTypeText", "adjustVolume", "cleanParam", "value", "executeCommand", "command", "Lcom/aaradhya/assistant/model/AppCommand;", "onResult", "fireIntent", "intent", "(Landroid/content/Intent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPrimeContacts", "", "Lkotlin/Pair;", "goBack", "goHome", "isAccessibilityEnabled", "launchApp", "appName", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "lockScreen", "lookupContactNumber", "name", "makeCall", "directNumber", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "muteDevice", "navigateTo", "destination", "openBatterySettings", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "openBluetoothSettings", "openPlayStore", "appQuery", "openRecents", "openSystemSettings", "openWhatsApp", "openWifiSettings", "openYoutubeFallback", "query", "playMusic", "playYoutube", "primeMsg", "index", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "savePrimeContacts", "contacts", "searchWeb", "sendSms", "message", "setAlarm", "hour", "minute", "label", "(IILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setTimer", "seconds", "(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "takeScreenshot", "toggleBluetooth", "on", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toggleFlashlight", "toggleWifi", "app_debug"})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> commandResult = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<android.content.Intent> actionIntent = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super android.content.Intent, kotlin.Unit> intentExecutor;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getCommandResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<android.content.Intent> getActionIntent() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<android.content.Intent, kotlin.Unit> getIntentExecutor() {
        return null;
    }
    
    public final void setIntentExecutor(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super android.content.Intent, kotlin.Unit> p0) {
    }
    
    private final java.lang.String cleanParam(java.lang.String value) {
        return null;
    }
    
    public final void executeCommand(@org.jetbrains.annotations.NotNull()
    com.aaradhya.assistant.model.AppCommand command, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onResult) {
    }
    
    private final java.lang.Object fireIntent(android.content.Intent intent, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object launchApp(java.lang.String appName, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object playMusic(java.lang.String query, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object playYoutube(java.lang.String query, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openYoutubeFallback(java.lang.String query, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object sendSms(java.lang.String name, java.lang.String message, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openWhatsApp(java.lang.String name, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> getPrimeContacts() {
        return null;
    }
    
    public final void savePrimeContacts(@org.jetbrains.annotations.NotNull()
    java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> contacts) {
    }
    
    private final java.lang.Object primeMsg(int index, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object makeCall(java.lang.String name, java.lang.String directNumber, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String adjustVolume(boolean up) {
        return null;
    }
    
    private final java.lang.String muteDevice() {
        return null;
    }
    
    private final java.lang.String toggleFlashlight(boolean on) {
        return null;
    }
    
    @kotlin.Suppress(names = {"DEPRECATION"})
    private final java.lang.Object toggleWifi(boolean on, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object toggleBluetooth(boolean on, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openSystemSettings(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openWifiSettings(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openBluetoothSettings(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openBatterySettings(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object setAlarm(int hour, int minute, java.lang.String label, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object setTimer(int seconds, java.lang.String label, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object navigateTo(java.lang.String destination, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object searchWeb(java.lang.String query, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object openPlayStore(java.lang.String appQuery, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String takeScreenshot() {
        return null;
    }
    
    private final java.lang.String lockScreen() {
        return null;
    }
    
    private final java.lang.String goHome() {
        return null;
    }
    
    private final java.lang.String goBack() {
        return null;
    }
    
    private final java.lang.String openRecents() {
        return null;
    }
    
    private final java.lang.String accessibilityScroll(boolean up) {
        return null;
    }
    
    private final java.lang.String accessibilityClickText(java.lang.String text) {
        return null;
    }
    
    private final java.lang.String accessibilityTypeText(java.lang.String text) {
        return null;
    }
    
    /**
     * Returns whether Accessibility service is currently active
     */
    public final boolean isAccessibilityEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String lookupContactNumber(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
}