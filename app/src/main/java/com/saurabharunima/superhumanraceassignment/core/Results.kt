package com.saurabharunima.superhumanraceassignment.core

import kotlinx.coroutines.CancellationException

/** Like runCatching, but never swallows coroutine cancellation. */
inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

class InvalidCredentialsException : Exception("Invalid email or password")
class EmailAlreadyUsedException : Exception("An account with this email already exists")
class NotFoundException(what: String) : Exception("$what not found")
