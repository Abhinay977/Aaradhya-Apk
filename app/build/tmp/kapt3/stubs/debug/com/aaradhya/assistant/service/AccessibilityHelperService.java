package com.aaradhya.assistant.service;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u0000 %2\u00020\u0001:\u0001%B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0012\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\nH\u0002J\u0012\u0010\f\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\nH\u0002J\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u000f\u001a\u00020\u000eJ\u0006\u0010\u0010\u001a\u00020\u000eJ\u0006\u0010\u0011\u001a\u00020\u000eJ\u0012\u0010\u0012\u001a\u00020\u000e2\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J\b\u0010\u0015\u001a\u00020\u000eH\u0016J\b\u0010\u0016\u001a\u00020\u000eH\u0016J\b\u0010\u0017\u001a\u00020\u000eH\u0014J\"\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\b2\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e0\u001bJ\u0010\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\nH\u0002J\u0006\u0010\u001d\u001a\u00020\u0006J\u0006\u0010\u001e\u001a\u00020\u0006J\u001e\u0010\u001f\u001a\u00020\u000e2\u0016\b\u0002\u0010 \u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e\u0018\u00010\u001bJ\u001e\u0010!\u001a\u00020\u000e2\u0016\b\u0002\u0010 \u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000e\u0018\u00010\u001bJ\u0006\u0010\"\u001a\u00020\u000eJ\u0010\u0010#\u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\bH\u0002J\u000e\u0010$\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/aaradhya/assistant/service/AccessibilityHelperService;", "Landroid/accessibilityservice/AccessibilityService;", "()V", "actionReceiver", "Landroid/content/BroadcastReceiver;", "clickOnText", "", "text", "", "findEditableNode", "Landroid/view/accessibility/AccessibilityNodeInfo;", "node", "findScrollableNode", "goBack", "", "goHome", "goToRecents", "lockScreen", "onAccessibilityEvent", "event", "Landroid/view/accessibility/AccessibilityEvent;", "onDestroy", "onInterrupt", "onServiceConnected", "openAppViaLauncher", "appLabel", "onResult", "Lkotlin/Function1;", "performClickOnNode", "scrollDown", "scrollUp", "swipeDown", "callback", "swipeUp", "takeScreenshot", "tryClickLabelVariants", "typeText", "Companion", "app_debug"})
public final class AccessibilityHelperService extends android.accessibilityservice.AccessibilityService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AccessibilityHelper";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SCREENSHOT = "com.aaradhya.assistant.TAKE_SCREENSHOT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_LOCK = "com.aaradhya.assistant.LOCK_SCREEN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_GO_HOME = "com.aaradhya.assistant.GO_HOME";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_GO_BACK = "com.aaradhya.assistant.GO_BACK";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_GO_RECENTS = "com.aaradhya.assistant.GO_RECENTS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SCROLL_UP = "com.aaradhya.assistant.SCROLL_UP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SCROLL_DOWN = "com.aaradhya.assistant.SCROLL_DOWN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_CLICK_TEXT = "com.aaradhya.assistant.CLICK_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_TYPE_TEXT = "com.aaradhya.assistant.TYPE_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_TEXT = "text";
    
    /**
     * Singleton — set in onServiceConnected, cleared in onDestroy
     */
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.aaradhya.assistant.service.AccessibilityHelperService instance;
    
    /**
     * Current foreground app package — updated on every window change event
     */
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.NotNull()
    private static volatile java.lang.String currentPackage = "";
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver actionReceiver = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.service.AccessibilityHelperService.Companion Companion = null;
    
    public AccessibilityHelperService() {
        super();
    }
    
    @java.lang.Override()
    protected void onServiceConnected() {
    }
    
    @java.lang.Override()
    public void onAccessibilityEvent(@org.jetbrains.annotations.Nullable()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    public final void goBack() {
    }
    
    public final void goHome() {
    }
    
    public final void goToRecents() {
    }
    
    public final void takeScreenshot() {
    }
    
    public final void lockScreen() {
    }
    
    /**
     * Finds a node containing [text] and clicks it (or its nearest clickable parent).
     * Returns true if a click was performed.
     */
    public final boolean clickOnText(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return false;
    }
    
    /**
     * Clicks a node — walks up the parent chain (up to 6 levels) to find a clickable ancestor.
     */
    private final boolean performClickOnNode(android.view.accessibility.AccessibilityNodeInfo node) {
        return false;
    }
    
    /**
     * Types [text] into the currently focused editable field.
     * Returns true if text was inserted.
     */
    public final boolean typeText(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return false;
    }
    
    /**
     * Scrolls the active window forward (down).
     */
    public final boolean scrollDown() {
        return false;
    }
    
    /**
     * Scrolls the active window backward (up).
     */
    public final boolean scrollUp() {
        return false;
    }
    
    /**
     * Performs a swipe-up gesture (bottom → top) — useful for scrolling or opening app drawer.
     */
    public final void swipeUp(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> callback) {
    }
    
    /**
     * Performs a swipe-down gesture (top → bottom) — useful for opening notification shade.
     */
    public final void swipeDown(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> callback) {
    }
    
    /**
     * Opens an app by going HOME and clicking its launcher icon.
     * Use as fallback when [packageManager.getLaunchIntentForPackage] returns null.
     *
     * Flow: HOME → wait 900ms → try multiple label variants → click
     * If not found on home screen, swipes up to open app drawer and tries again.
     *
     * Label variants tried (in priority order):
     * 1. Full label (e.g. "WhatsApp Business")
     * 2. First word (e.g. "WhatsApp")
     * 3. Last word (e.g. "Business")
     * 4. Each individual word >= 4 chars
     */
    public final void openAppViaLauncher(@org.jetbrains.annotations.NotNull()
    java.lang.String appLabel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onResult) {
    }
    
    /**
     * Builds a prioritized list of label variants and clicks the first one found on screen.
     * Returns true if any variant was clicked successfully.
     */
    private final boolean tryClickLabelVariants(java.lang.String appLabel) {
        return false;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findEditableNode(android.view.accessibility.AccessibilityNodeInfo node) {
        return null;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findScrollableNode(android.view.accessibility.AccessibilityNodeInfo node) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0019\u001a\u00020\u001aR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\"\u0010\u0014\u001a\u0004\u0018\u00010\u00132\b\u0010\u000f\u001a\u0004\u0018\u00010\u0013@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006\u001d"}, d2 = {"Lcom/aaradhya/assistant/service/AccessibilityHelperService$Companion;", "", "()V", "ACTION_CLICK_TEXT", "", "ACTION_GO_BACK", "ACTION_GO_HOME", "ACTION_GO_RECENTS", "ACTION_LOCK", "ACTION_SCREENSHOT", "ACTION_SCROLL_DOWN", "ACTION_SCROLL_UP", "ACTION_TYPE_TEXT", "EXTRA_TEXT", "TAG", "<set-?>", "currentPackage", "getCurrentPackage", "()Ljava/lang/String;", "Lcom/aaradhya/assistant/service/AccessibilityHelperService;", "instance", "getInstance", "()Lcom/aaradhya/assistant/service/AccessibilityHelperService;", "isEnabled", "", "context", "Landroid/content/Context;", "openAccessibilitySettings", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Singleton — set in onServiceConnected, cleared in onDestroy
         */
        @org.jetbrains.annotations.Nullable()
        public final com.aaradhya.assistant.service.AccessibilityHelperService getInstance() {
            return null;
        }
        
        /**
         * Current foreground app package — updated on every window change event
         */
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCurrentPackage() {
            return null;
        }
        
        /**
         * Returns true if the Accessibility Service is enabled in system settings.
         * Call this before trying to use the service.
         */
        public final boolean isEnabled(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
        
        /**
         * Opens the Accessibility Settings page so the user can enable the service.
         */
        public final void openAccessibilitySettings(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}