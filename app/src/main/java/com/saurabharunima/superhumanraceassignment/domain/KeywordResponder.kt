package com.saurabharunima.superhumanraceassignment.domain

/**
 * Rule-based chatbot. Single words match whole tokens (so "hi" does not match "this");
 * multi-word keywords match as substrings. First matching rule wins.
 */
class KeywordResponder(
    private val rules: List<Pair<List<String>, String>>,
    private val fallback: String,
) {
    fun respond(input: String): String {
        val text = input.lowercase()
        val tokens = text.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }.toSet()
        return rules.firstOrNull { (keywords, _) ->
            keywords.any { k ->
                val key = k.lowercase()
                if (key.contains(' ')) text.contains(key) else key in tokens
            }
        }?.second ?: fallback
    }
}
