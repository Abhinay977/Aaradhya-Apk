package com.aaradhya.assistant.apps

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Launches an installed Android app by its human-readable name.
 *
 * Uses [InstalledAppsManager] for fuzzy app discovery, then fires the
 * standard launcher intent with FLAG_ACTIVITY_NEW_TASK (required when
 * launching from a non-Activity context such as a ViewModel or Service).
 */
object AppLauncher {

    private const val TAG = "AppLauncher"

    /**
     * Finds the best-matching installed app for [appName] and opens it.
     *
     * @param context  Android context (Application context is fine)
     * @param appName  Human-readable app name (e.g. "YouTube", "WhatsApp")
     * @return         `true` if the app was found and the intent was fired,
     *                 `false` if no matching app was found or launch failed
     */
    fun launch(context: Context, appName: String): Boolean {
        if (appName.isBlank()) {
            Log.w(TAG, "launch() called with blank app name")
            return false
        }

        val intent = InstalledAppsManager.getLaunchIntent(context, appName) ?: run {
            Log.w(TAG, "No launch intent found for: \"$appName\"")
            return false
        }

        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context.startActivity(intent)
            Log.d(TAG, "Launched: \"$appName\" → ${intent.`package`}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity for \"$appName\": ${e.message}", e)
            false
        }
    }
}
