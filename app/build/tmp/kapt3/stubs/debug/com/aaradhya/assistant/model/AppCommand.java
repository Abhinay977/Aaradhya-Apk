package com.aaradhya.assistant.model;

/**
 * Represents a parsed voice or text command from the user.
 * @param type The command type identifier (e.g., "SMS", "VOLUME_UP", etc.)
 * @param params Additional parameters for the command
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 \u00142\u00020\u0001:\u0001\u0014B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0014\b\u0002\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u0015\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J)\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u0014\b\u0002\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0003H\u00d6\u0001R\u001d\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0015"}, d2 = {"Lcom/aaradhya/assistant/model/AppCommand;", "", "type", "", "params", "", "(Ljava/lang/String;Ljava/util/Map;)V", "getParams", "()Ljava/util/Map;", "getType", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "Companion", "app_debug"})
public final class AppCommand {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String type = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.String> params = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_APP = "OPEN_APP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SMS = "SMS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String WHATSAPP_MSG = "WHATSAPP_MSG";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PRIME_MSG = "PRIME_MSG";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MAKE_CALL = "MAKE_CALL";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String VOLUME_UP = "VOLUME_UP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String VOLUME_DOWN = "VOLUME_DOWN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String VOLUME_MUTE = "VOLUME_MUTE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String FLASHLIGHT_ON = "FLASHLIGHT_ON";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String FLASHLIGHT_OFF = "FLASHLIGHT_OFF";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String WIFI_ON = "WIFI_ON";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String WIFI_OFF = "WIFI_OFF";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BT_ON = "BLUETOOTH_ON";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BT_OFF = "BLUETOOTH_OFF";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_SETTINGS = "OPEN_SETTINGS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_WIFI_SETTINGS = "OPEN_WIFI_SETTINGS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_BT_SETTINGS = "OPEN_BT_SETTINGS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_BATTERY_SETTINGS = "OPEN_BATTERY_SETTINGS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_ALARM = "SET_ALARM";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_TIMER = "SET_TIMER";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PLAY_MUSIC = "PLAY_MUSIC";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PLAY_YOUTUBE = "PLAY_YOUTUBE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String NAVIGATE_TO = "NAVIGATE_TO";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SEARCH_WEB = "SEARCH_WEB";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_PLAY_STORE = "OPEN_PLAY_STORE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAKE_SCREENSHOT = "TAKE_SCREENSHOT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LOCK_SCREEN = "LOCK_SCREEN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GO_HOME = "GO_HOME";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GO_BACK = "GO_BACK";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPEN_RECENTS = "OPEN_RECENTS";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SCROLL_UP = "SCROLL_UP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SCROLL_DOWN = "SCROLL_DOWN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CLICK_TEXT = "CLICK_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_TEXT = "TYPE_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.model.AppCommand.Companion Companion = null;
    
    public AppCommand(@org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> params) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> getParams() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.aaradhya.assistant.model.AppCommand copy(@org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> params) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\"\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/aaradhya/assistant/model/AppCommand$Companion;", "", "()V", "BT_OFF", "", "BT_ON", "CLICK_TEXT", "FLASHLIGHT_OFF", "FLASHLIGHT_ON", "GO_BACK", "GO_HOME", "LOCK_SCREEN", "MAKE_CALL", "NAVIGATE_TO", "OPEN_APP", "OPEN_BATTERY_SETTINGS", "OPEN_BT_SETTINGS", "OPEN_PLAY_STORE", "OPEN_RECENTS", "OPEN_SETTINGS", "OPEN_WIFI_SETTINGS", "PLAY_MUSIC", "PLAY_YOUTUBE", "PRIME_MSG", "SCROLL_DOWN", "SCROLL_UP", "SEARCH_WEB", "SET_ALARM", "SET_TIMER", "SMS", "TAKE_SCREENSHOT", "TYPE_TEXT", "VOLUME_DOWN", "VOLUME_MUTE", "VOLUME_UP", "WHATSAPP_MSG", "WIFI_OFF", "WIFI_ON", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}