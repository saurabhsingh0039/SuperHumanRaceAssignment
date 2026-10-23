package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.EmailAlreadyUsedException
import com.saurabharunima.superhumanraceassignment.core.InvalidCredentialsException
import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.NotFoundException
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.local.SessionStorage
import com.saurabharunima.superhumanraceassignment.data.model.SocialProvider
import com.saurabharunima.superhumanraceassignment.data.model.User
import com.saurabharunima.superhumanraceassignment.data.remote.UserDto
import com.saurabharunima.superhumanraceassignment.data.remote.toUser
import com.saurabharunima.superhumanraceassignment.data.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@Singleton
class MockAuthRepository @Inject constructor(
    private val store: UserStore,
    private val storage: SessionStorage,
) : AuthRepository {

    override val sessionUserId: StateFlow<String?> = store.session

    override suspend fun login(email: String, password: String): Result<User> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val match = store.all().firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        } ?: throw InvalidCredentialsException()
        startSession(match.id)
        match.toUser()
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> =
        suspendRunCatching {
            delay(NETWORK_DELAY_MS)
            if (store.all().any { it.email.equals(email.trim(), ignoreCase = true) }) {
                throw EmailAlreadyUsedException()
            }
            val user = UserDto(
                id = "u_${UUID.randomUUID()}",
                name = name.trim(),
                email = email.trim(),
                password = password,
                xp = 0, seeds = 0, gems = 0, streakDays = 0,
            )
            store.add(user)
            startSession(user.id)
            user.toUser()
        }

    override suspend fun loginWithProvider(provider: SocialProvider): Result<User> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val id = store.currentUserId()
        val user = store.all().firstOrNull { it.id == id } ?: throw NotFoundException("User")
        startSession(user.id)
        user.toUser()
    }

    override suspend fun restoreSession() {
        suspendRunCatching {
            val saved = storage.userId.first() ?: return@suspendRunCatching
            if (store.all().any { it.id == saved }) store.setSession(saved) else storage.clear()
        }
    }

    override suspend fun logout() {
        store.setSession(null)
        storage.clear()
    }

    private suspend fun startSession(userId: String) {
        store.setSession(userId)
        storage.save(userId)
    }
}
