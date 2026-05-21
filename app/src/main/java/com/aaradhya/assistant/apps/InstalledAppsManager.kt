package com.aaradhya.assistant.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

/**
 * Lightweight model representing a launchable installed app.
 */
data class AppModel(
    val name: String,        // Lowercase, space-stripped (e.g. "youtube")
    val nameWords: List<String>, // Lowercase words split by space (e.g. ["youtube"])
    val label: String,       // Original display label (e.g. "YouTube")
    val packageName: String  // e.g. "com.google.android.youtube"
)

/**
 * Scans, caches, and fuzzy-searches all installed launchable apps on the device.
 *
 * Search tiers (in priority order):
 *  1. Exact match     — query == app name
 *  2. Contains match  — app name contains query, or query contains app name
 *  3. Partial word    — any individual word in the query matches any word in the app name
 */
object InstalledAppsManager {

    private const val TAG = "InstalledAppsManager"

    /** In-memory cache so repeated lookups don't re-query PackageManager */
    @Volatile
    private var cachedApps: List<AppModel>? = null

    /**
     * Returns all launchable apps. Uses cache on second call onwards.
     * Call [clearCache] to force a refresh (e.g. after an app install/uninstall).
     */
    fun getAllApps(context: Context): List<AppModel> {
        cachedApps?.let { return it }

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Pass 0 instead of MATCH_ALL to avoid OEM-specific query bugs on Android 11+
        val apps = pm.queryIntentActivities(mainIntent, 0)
            .mapNotNull { resolveInfo ->
                try {
                    val label = resolveInfo.loadLabel(pm).toString()
                    val pkg   = resolveInfo.activityInfo.packageName
                    val labelLower = label.lowercase().trim()
                    val app = AppModel(
                        name        = labelLower.replace(" ", ""),
                        nameWords   = labelLower.split(Regex("\\s+")).filter { it.isNotEmpty() },
                        label       = label,
                        packageName = pkg
                    )
                    Log.d(TAG, "Installed App: ${app.label}")
                    app
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read app info: ${e.message}")
                    null
                }
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name }

        Log.d(TAG, "Scanned ${apps.size} installed launchable apps")
        if (apps.isNotEmpty()) {
            cachedApps = apps
        }
        return apps
    }

    /** Clears the cached app list so the next call to [getAllApps] re-scans. */
    fun clearCache() {
        cachedApps = null
        Log.d(TAG, "App cache cleared")
    }

    /**
     * 3-tier fuzzy search for an app by user-provided name.
     *
     * @param context  Android context
     * @param query    The name the user said / Gemini returned (e.g. "YouTube", "whatsapp")
     * @return         Best-matching [AppModel], or null if nothing found
     */
    fun findApp(context: Context, query: String): AppModel? {
        val normalized = query.lowercase().trim().replace(" ", "")
        if (normalized.isEmpty()) return null

        val apps = getAllApps(context)

        // ── Tier 1: Exact match ────────────────────────────────────────────────
        apps.find { it.name == normalized || it.packageName.contains(normalized) }
            ?.let { return it }

        // ── Tier 2: Contains match ─────────────────────────────────────────────
        apps.find { it.name.contains(normalized) || normalized.contains(it.name) }
            ?.let { return it }

        // ── Tier 3: Partial word match ─────────────────────────────────────────
        // Split query into words and check against stored nameWords.
        // This allows "you tube" → "YouTube", "insta" → "Instagram"
        val queryWords = query.lowercase().trim().split(Regex("\\s+")).filter { it.length >= 3 }
        if (queryWords.isNotEmpty()) {
            apps.find { app ->
                queryWords.any { qWord ->
                    app.nameWords.any { aWord -> aWord.startsWith(qWord) || qWord.startsWith(aWord) }
                }
            }?.let { return it }
        }

        // ── Tier 4: Prefix match on stripped name ──────────────────────────────
        // Handles "insta" → "instagram", "spotify" → "spotify"
        return apps.find { app ->
            app.name.startsWith(normalized) || normalized.startsWith(app.name)
        }
    }

    /**
     * Finds a launchable app and returns its Android launch [Intent], or null if not found.
     *
     * @param context  Android context
     * @param appName  App name to search for
     */
    fun getLaunchIntent(context: Context, appName: String): Intent? {
        val app = findApp(context, appName) ?: run {
            Log.w(TAG, "No app found matching: \"$appName\"")
            return null
        }
        Log.d(TAG, "Matched \"$appName\" → ${app.label} (${app.packageName})")
        return context.packageManager.getLaunchIntentForPackage(app.packageName)
    }
}
