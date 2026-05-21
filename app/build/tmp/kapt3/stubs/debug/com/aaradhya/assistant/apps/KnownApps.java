package com.aaradhya.assistant.apps;

/**
 * Curated map of common app aliases → exact Android package names.
 *
 * Purpose: Fast-path lookup BEFORE scanning all installed apps.
 * This eliminates label-matching fragility for the most popular apps.
 *
 * Keys: lowercase, no spaces, common user aliases (voice-friendly)
 * Values: canonical Android package name
 *
 * Usage:
 *  val pkg = KnownApps.resolvePackage("whatsapp")  // → "com.whatsapp"
 *  val pkg = KnownApps.resolvePackage("insta")     // → "com.instagram.android"
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0007\u001a\u00020\u0005R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/aaradhya/assistant/apps/KnownApps;", "", "()V", "packageMap", "", "", "resolvePackage", "query", "app_debug"})
public final class KnownApps {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.String> packageMap = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.apps.KnownApps INSTANCE = null;
    
    private KnownApps() {
        super();
    }
    
    /**
     * Returns the package name for a known app alias, or null if not in the map.
     * The [query] is normalized (lowercased, spaces stripped) before lookup.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String resolvePackage(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
}