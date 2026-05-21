package com.aaradhya.assistant.service;

/**
 * Foreground service showing a draggable floating orb overlay.
 * Triggered by double-press power button.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\fH\u0002J\u0014\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0016J\b\u0010\u0012\u001a\u00020\nH\u0016J\b\u0010\u0013\u001a\u00020\nH\u0016J\"\u0010\u0014\u001a\u00020\f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\fH\u0016J\b\u0010\u0017\u001a\u00020\nH\u0002J\b\u0010\u0018\u001a\u00020\nH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/aaradhya/assistant/service/AARADHYAOverlayService;", "Landroid/app/Service;", "()V", "overlayView", "Landroid/view/View;", "windowManager", "Landroid/view/WindowManager;", "buildNotification", "Landroid/app/Notification;", "createNotificationChannel", "", "dpToPx", "", "dp", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "flags", "startId", "removeOverlay", "showOverlay", "Companion", "app_debug"})
public final class AARADHYAOverlayService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "OverlayService";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "aaradhya_overlay_channel";
    private static final int NOTIF_ID = 1;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SHOW = "SHOW_OVERLAY";
    @kotlin.jvm.Volatile()
    private static volatile boolean isRunning = false;
    @org.jetbrains.annotations.Nullable()
    private android.view.WindowManager windowManager;
    @org.jetbrains.annotations.Nullable()
    private android.view.View overlayView;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.service.AARADHYAOverlayService.Companion Companion = null;
    
    public AARADHYAOverlayService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    private final void showOverlay() {
    }
    
    private final void removeOverlay() {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final android.app.Notification buildNotification() {
        return null;
    }
    
    private final int dpToPx(int dp) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\u000b\"\u0004\b\f\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lcom/aaradhya/assistant/service/AARADHYAOverlayService$Companion;", "", "()V", "ACTION_SHOW", "", "CHANNEL_ID", "NOTIF_ID", "", "TAG", "isRunning", "", "()Z", "setRunning", "(Z)V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isRunning() {
            return false;
        }
        
        public final void setRunning(boolean p0) {
        }
    }
}