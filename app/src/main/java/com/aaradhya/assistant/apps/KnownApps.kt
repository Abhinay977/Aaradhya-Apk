package com.aaradhya.assistant.apps

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
 *   val pkg = KnownApps.resolvePackage("whatsapp")  // → "com.whatsapp"
 *   val pkg = KnownApps.resolvePackage("insta")     // → "com.instagram.android"
 */
object KnownApps {

    /**
     * Returns the package name for a known app alias, or null if not in the map.
     * The [query] is normalized (lowercased, spaces stripped) before lookup.
     */
    fun resolvePackage(query: String): String? {
        val key = query.lowercase().trim().replace(" ", "")
        // Direct hit
        packageMap[key]?.let { return it }
        // Prefix / contains scan over the map keys for short aliases like "insta", "yt"
        return packageMap.entries.firstOrNull { (k, _) ->
            k.startsWith(key) || key.startsWith(k)
        }?.value
    }

    // ── Master map ────────────────────────────────────────────────────────────
    // Add as many aliases as needed — duplicates for the same package are fine.

    private val packageMap: Map<String, String> = mapOf(

        // ── Social / Messaging ───────────────────────────────────────────────
        "whatsapp"          to "com.whatsapp",
        "whatsappbusiness"  to "com.whatsapp.w4b",
        "instagram"         to "com.instagram.android",
        "insta"             to "com.instagram.android",
        "facebook"          to "com.facebook.katana",
        "fb"                to "com.facebook.katana",
        "messenger"         to "com.facebook.orca",
        "telegram"          to "org.telegram.messenger",
        "snapchat"          to "com.snapchat.android",
        "snap"              to "com.snapchat.android",
        "twitter"           to "com.twitter.android",
        "x"                 to "com.twitter.android",
        "linkedin"          to "com.linkedin.android",
        "discord"           to "com.discord",
        "reddit"            to "com.reddit.frontpage",
        "pinterest"         to "com.pinterest",
        "threads"           to "com.instagram.barcelona",
        "signal"            to "org.thoughtcrime.securesms",
        "viber"             to "com.viber.voip",
        "skype"             to "com.skype.raider",

        // ── Google Apps ──────────────────────────────────────────────────────
        "youtube"           to "com.google.android.youtube",
        "yt"                to "com.google.android.youtube",
        "youtubemusic"      to "com.google.android.apps.youtube.music",
        "ytmusic"           to "com.google.android.apps.youtube.music",
        "gmail"             to "com.google.android.gm",
        "googlemaps"        to "com.google.android.apps.maps",
        "maps"              to "com.google.android.apps.maps",
        "googledrive"       to "com.google.android.apps.docs",
        "drive"             to "com.google.android.apps.docs",
        "googlephotos"      to "com.google.android.apps.photos",
        "photos"            to "com.google.android.apps.photos",
        "googlechrome"      to "com.android.chrome",
        "chrome"            to "com.android.chrome",
        "googlemeet"        to "com.google.android.apps.meetings",
        "meet"              to "com.google.android.apps.meetings",
        "googletranslate"   to "com.google.android.apps.translate",
        "translate"         to "com.google.android.apps.translate",
        "googledocs"        to "com.google.android.apps.docs.editors.docs",
        "googlesheets"      to "com.google.android.apps.docs.editors.sheets",
        "googleslides"      to "com.google.android.apps.docs.editors.slides",
        "googlecalendar"    to "com.google.android.calendar",
        "calendar"          to "com.google.android.calendar",
        "googlekeep"        to "com.google.android.keep",
        "keep"              to "com.google.android.keep",
        "googleplay"        to "com.android.vending",
        "playstore"         to "com.android.vending",
        "googleassistant"   to "com.google.android.googlequicksearchbox",
        "googlesearch"      to "com.google.android.googlequicksearchbox",

        // ── Streaming / Entertainment ────────────────────────────────────────
        "netflix"           to "com.netflix.mediaclient",
        "spotify"           to "com.spotify.music",
        "amazonprime"       to "com.amazon.avod.thirdpartyclient",
        "primevideo"        to "com.amazon.avod.thirdpartyclient",
        "hotstar"           to "in.startv.hotstar",
        "disneyplus"        to "com.disney.disneyplus",
        "disney"            to "com.disney.disneyplus",
        "jiosaavn"          to "com.jio.media.jiobeats",
        "saavn"             to "com.jio.media.jiobeats",
        "gaana"             to "com.gaana",
        "wynk"              to "com.bsbportal.music",
        "amazonmusic"       to "com.amazon.mp3",
        "applemusic"        to "com.apple.android.music",
        "soundcloud"        to "com.soundcloud.android",
        "twitch"            to "tv.twitch.android.app",
        "zee5"              to "com.graymatrix.did",
        "sonyliv"           to "com.sonyliv",
        "mxplayer"          to "com.mxtech.videoplayer.ad",
        "vlc"               to "org.videolan.vlc",

        // ── Shopping / Finance ───────────────────────────────────────────────
        "amazon"            to "in.amazon.mShop.android.shopping",
        "flipkart"          to "com.flipkart.android",
        "myntra"            to "com.myntra.android",
        "meesho"            to "com.meesho.supply",
        "swiggy"            to "in.swiggy.android",
        "zomato"            to "app.zomato.android",
        "phonepe"           to "com.phonepe.app",
        "googlepay"         to "com.google.android.apps.nbu.paisa.user",
        "gpay"              to "com.google.android.apps.nbu.paisa.user",
        "paytm"             to "net.one97.paytm",
        "amazonpay"         to "in.amazon.mShop.android.shopping",
        "bhim"              to "in.org.npci.upiapp",

        // ── Utilities ────────────────────────────────────────────────────────
        "calculator"        to "com.google.android.calculator",
        "calc"              to "com.google.android.calculator",
        "clock"             to "com.google.android.deskclock",
        "camera"            to "com.android.camera2",
        "gallery"           to "com.google.android.apps.photos",
        "contacts"          to "com.google.android.contacts",
        "phone"             to "com.google.android.dialer",
        "dialer"            to "com.google.android.dialer",
        "messages"          to "com.google.android.apps.messaging",
        "sms"               to "com.google.android.apps.messaging",
        "files"             to "com.google.android.documentsui",
        "filemanager"       to "com.google.android.documentsui",
        "settings"          to "com.android.settings",

        // ── Productivity ─────────────────────────────────────────────────────
        "zoom"              to "us.zoom.videomeetings",
        "microsoftteams"    to "com.microsoft.teams",
        "teams"             to "com.microsoft.teams",
        "microsoftoutlook"  to "com.microsoft.office.outlook",
        "outlook"           to "com.microsoft.office.outlook",
        "microsoftword"     to "com.microsoft.office.word",
        "microsoftexcel"    to "com.microsoft.office.excel",
        "microsoftpowerpoint" to "com.microsoft.office.powerpoint",
        "notion"            to "notion.id",
        "evernote"          to "com.evernote",
        "todoist"           to "com.todoist.android.Todoist",

        // ── News / Reading ───────────────────────────────────────────────────
        "inshorts"          to "com.nis.android",
        "dailyhunt"         to "com.eterno",
        "sharechat"         to "in.sharechat.sharechat",

        // ── Travel ───────────────────────────────────────────────────────────
        "makemytrip"        to "com.makemytrip",
        "mmt"               to "com.makemytrip",
        "irctc"             to "cris.org.in.prs.ima",
        "ola"               to "com.olacabs.customer",
        "uber"              to "com.ubercab",
        "rapido"            to "com.rapido.passenger",
        "nammayatri"        to "net.opentripplanner.android",
    )
}
