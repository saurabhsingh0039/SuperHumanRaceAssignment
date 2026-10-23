package com.saurabharunima.superhumanraceassignment.domain

/**
 * Bot text is shown as plain text, so markdown markers must not leak into bubbles
 * (the reference app showed a literal **CARBON FOOTPRINT**). Pure, so it is unit-testable.
 */
object MarkdownStripper {
    private val markers = Regex("""\*\*|__|`""")
    private val heading = Regex("""(?m)^\s{0,3}#{1,6}\s+""")

    fun plain(text: String): String = text.replace(heading, "").replace(markers, "").trim()
}
