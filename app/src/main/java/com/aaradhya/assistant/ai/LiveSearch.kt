package com.aaradhya.assistant.ai

import android.net.Uri
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LiveSearch {
    private const val TAG = "LiveSearch"

    private val LIVE_KEYWORDS = listOf(
        "current", "latest", "today", "live", "news", 
        "weather", "price", "score", "cm", "pm"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if the query needs a live internet search.
     * Also ensures we don't infinitely loop by checking if the query
     * already contains our injected "Live Internet Information:" prompt.
     */
    fun needsLiveSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        // Prevent infinite looping if the text already contains the injected data
        if (lowerQuery.contains("live internet information:")) {
            return false
        }
        
        for (word in LIVE_KEYWORDS) {
            if (lowerQuery.contains(word)) {
                return true
            }
        }
        return false
    }

    /**
     * Searches the web using Serpstack API and returns a formatted string with top results.
     */
    fun searchWeb(query: String, apiKey: String): String {
        if (apiKey.isBlank()) {
            return "Serpstack API key is missing. Please add it in settings."
        }

        try {
            val url = "http://api.serpstack.com/search?access_key=$apiKey&query=${Uri.encode(query)}&num=3&output=json"
            val request = Request.Builder().url(url).build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return "No response from search API."
            
            val data = JSONObject(responseBody)
            
            if (data.has("error")) {
                val errorMsg = data.optJSONObject("error")?.optString("info") ?: "Unknown API Error"
                Log.e(TAG, "Search API error: $errorMsg")
                return "Search API error: $errorMsg"
            }

            val results = data.optJSONArray("organic_results")
            if (results == null || results.length() == 0) {
                return "No live information found."
            }

            val searchText = java.lang.StringBuilder()
            for (i in 0 until Math.min(results.length(), 3)) {
                val result = results.getJSONObject(i)
                val title = result.optString("title", "")
                val snippet = result.optString("snippet", "")
                searchText.append(title).append("\n").append(snippet).append("\n\n")
            }
            return searchText.toString().trim()

        } catch (e: Exception) {
            Log.e(TAG, "Error searching web: ${e.message}")
            return "Internet connection problem."
        }
    }
}
