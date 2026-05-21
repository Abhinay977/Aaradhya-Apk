package com.aaradhya.assistant.apps;

/**
 * Scans, caches, and fuzzy-searches all installed launchable apps on the device.
 *
 * Search tiers (in priority order):
 * 1. Exact match     — query == app name
 * 2. Contains match  — app name contains query, or query contains app name
 * 3. Partial word    — any individual word in the query matches any word in the app name
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\b\u001a\u00020\tJ\u0018\u0010\n\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u000b\u001a\u00020\fJ\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/aaradhya/assistant/apps/InstalledAppsManager;", "", "()V", "TAG", "", "cachedApps", "", "Lcom/aaradhya/assistant/apps/AppModel;", "clearCache", "", "findApp", "context", "Landroid/content/Context;", "query", "getAllApps", "getLaunchIntent", "Landroid/content/Intent;", "appName", "app_debug"})
public final class InstalledAppsManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "InstalledAppsManager";
    
    /**
     * In-memory cache so repeated lookups don't re-query PackageManager
     */
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.util.List<com.aaradhya.assistant.apps.AppModel> cachedApps;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.apps.InstalledAppsManager INSTANCE = null;
    
    private InstalledAppsManager() {
        super();
    }
    
    /**
     * Returns all launchable apps. Uses cache on second call onwards.
     * Call [clearCache] to force a refresh (e.g. after an app install/uninstall).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.aaradhya.assistant.apps.AppModel> getAllApps(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Clears the cached app list so the next call to [getAllApps] re-scans.
     */
    public final void clearCache() {
    }
    
    /**
     * 3-tier fuzzy search for an app by user-provided name.
     *
     * @param context  Android context
     * @param query    The name the user said / Gemini returned (e.g. "YouTube", "whatsapp")
     * @return         Best-matching [AppModel], or null if nothing found
     */
    @org.jetbrains.annotations.Nullable()
    public final com.aaradhya.assistant.apps.AppModel findApp(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    /**
     * Finds a launchable app and returns its Android launch [Intent], or null if not found.
     *
     * @param context  Android context
     * @param appName  App name to search for
     */
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent getLaunchIntent(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String appName) {
        return null;
    }
}