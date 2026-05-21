fun main() {
    val t = "open youtube"
    val openRegex = Regex(
        "(?:open|launch|start|play|chalu karo|chalu kar)\\s+(.+)|" +
        "(.+?)\\s+(?:kholo|khol do|open karo|open kar|launch karo|chalu karo|chalu kar do|start karo|play karo)"
    )
    openRegex.find(t)?.let { m ->
        val g1 = m.groupValues.getOrNull(1) ?: ""
        val g2 = m.groupValues.getOrNull(2) ?: ""
        val app = (g1 + g2).trim().replace(Regex("\\s+(please|now|for me|yaar|jaldi|na|zara)$"), "")
        println("Match: '$app'")
        println("Groups: ${m.groupValues}")
    }
}
