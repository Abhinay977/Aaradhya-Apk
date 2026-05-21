package com.aaradhya.assistant.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0005J\u0016\u0010\f\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u0005R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/aaradhya/assistant/ai/LiveSearch;", "", "()V", "LIVE_KEYWORDS", "", "", "TAG", "client", "Lokhttp3/OkHttpClient;", "needsLiveSearch", "", "query", "searchWeb", "apiKey", "app_debug"})
public final class LiveSearch {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "LiveSearch";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> LIVE_KEYWORDS = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.ai.LiveSearch INSTANCE = null;
    
    private LiveSearch() {
        super();
    }
    
    /**
     * Checks if the query needs a live internet search.
     * Also ensures we don't infinitely loop by checking if the query
     * already contains our injected "Live Internet Information:" prompt.
     */
    public final boolean needsLiveSearch(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return false;
    }
    
    /**
     * Searches the web using Serpstack API and returns a formatted string with top results.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String searchWeb(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey) {
        return null;
    }
}