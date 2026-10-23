package com.saurabharunima.superhumanraceassignment.domain

/** Returns an error message, or null when valid. No android.* deps, so it runs in JVM unit tests. */
object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 8
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun email(value: String): String? =
        if (EMAIL.matches(value.trim())) null else "Enter a valid email address"

    fun password(value: String): String? =
        if (value.length >= MIN_PASSWORD_LENGTH) null
        else "Password must be at least $MIN_PASSWORD_LENGTH characters"

    fun name(value: String): String? =
        if (value.trim().length >= 2) null else "Enter your name"
}
