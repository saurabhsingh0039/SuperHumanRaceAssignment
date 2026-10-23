package com.saurabharunima.superhumanraceassignment.domain

/** Pure, so it runs in JVM unit tests. */
object TimeAgo {
    fun format(nowMillis: Long, thenMillis: Long): String {
        val minutes = ((nowMillis - thenMillis).coerceAtLeast(0)) / 60_000L
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            else -> "${minutes / (60 * 24)}d ago"
        }
    }
}
