package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.NotFoundException
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.remote.toStats
import com.saurabharunima.superhumanraceassignment.data.remote.toUser
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import com.saurabharunima.superhumanraceassignment.domain.AuthValidator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockUserRepository @Inject constructor(
    private val store: UserStore,
) : UserRepository {

    override suspend fun getProfile(): Result<Profile> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val id = store.currentUserId()
        val dto = store.all().firstOrNull { it.id == id } ?: throw NotFoundException("User")
        Profile(dto.toUser(), dto.toStats())
    }

    override suspend fun updateName(name: String): Result<Profile> = suspendRunCatching {
        AuthValidator.name(name)?.let { throw IllegalArgumentException(it) }
        delay(NETWORK_DELAY_MS)
        val dto = store.update(store.currentUserId()) { it.copy(name = name.trim()) }
        Profile(dto.toUser(), dto.toStats())
    }
}
